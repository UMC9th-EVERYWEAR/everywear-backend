package com.umc.EveryWear.domain.product.service.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.EveryWear.domain.product.converter.ProductConverter;
import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.enums.ShoppingMall;
import com.umc.EveryWear.domain.product.exception.ProductException;
import com.umc.EveryWear.domain.product.exception.code.ProductErrorCode;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserProductRepository userProductRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${FASTAPI_BASE_URL}")
    private String fastApiBaseUrl;

    private static final int CRAWL_TIMEOUT_SECONDS = 120;

    private ShoppingMall detectShoppingMall(String url) {
        if (url == null) return null;
        for (ShoppingMall mall : ShoppingMall.values()) {
            if (mall.matchesUrl(url)) return mall;
        }
        return null;
    }

    @Override
    public ProductResDTO.ImportResult importProduct(Long userId, ProductReqDTO.ImportDTO dto) {
        String productUrl = dto.getProduct_url();
        ShoppingMall mall = detectShoppingMall(productUrl);
        if (mall == null) {
            throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
        }
        ProductResDTO.ImportDTO result = switch (mall) {
            case MUSINSA -> importMusinsaProduct(userId, ProductReqDTO.ImportMusinsaDTO.builder().product_url(productUrl).build());
            case ZIGZAG -> importZigzagProduct(userId, ProductReqDTO.ImportZigzagDTO.builder().product_url(productUrl).build());
            case CM29 -> import29cmProduct(userId, ProductReqDTO.Import29cmDTO.builder().product_url(productUrl).build());
            case WCONCEPT -> importWconceptProduct(userId, ProductReqDTO.ImportWconceptDTO.builder().product_url(productUrl).build());
        };
        return ProductResDTO.ImportResult.builder().dto(result).mall(mall).build();
    }

    // 쇼핑몰 공통 import
    private ProductResDTO.ImportDTO doImportByMall(Long userId, String productUrl, ShoppingMall mall) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ProductException(ProductErrorCode.CRAWLING_FAILED));
            if (!mall.matchesUrl(productUrl)) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }

            // 1. 리다이렉트가 필요하면 최종 URL 확보
            String finalUrl = ProductUrlUtil.resolveRedirect(productUrl);

            // 2. 최종 URL에서 상품 고유 번호(product_num) 추출
            Long productNum = ProductUrlUtil.extractProductNumFromFinalUrl(finalUrl, mall);

            // 3. product_num으로 DB 존재 여부 판단
            if (productNum != null) {
                Product existingByNum = productRepository.findByProductNum(productNum).orElse(null);
                if (existingByNum != null) {
                    // DB에 있으면 크롤링 없이 user_product에만 연결
                    return resolveOrLinkUserProduct(userId, user, existingByNum, true);
                }
            }

            // 4. DB에 없으면 크롤링 후 상품 저장 및 user_product 연결
            ProductCrawlingData crawlerData = crawlProduct(productUrl, mall);
            String canonicalUrl = ProductUrlUtil.canonicalizeProductUrl(crawlerData.getProductUrl(), mall);
            crawlerData.setProductUrl(canonicalUrl);
            try {
                return createProductAndLink(user, crawlerData);
            } catch (DataIntegrityViolationException e) {
                // product_num 중복(동시 등록 등)
                if (isDuplicateProductNumConstraint(e)) {
                    // 직전 flush에서 실패한 엔티티들이 다시 flush되지 않도록 영속성 컨텍스트 초기화
                    if (entityManager != null) {
                        entityManager.clear();
                    }
                    return handleDuplicateProductNum(userId, user, crawlerData.getProductNum());
                }
                throw e;
            }
        } catch (ProductException e) {
            throw e;
        } catch (Exception e) {
            log.error("{} 상품 크롤링 중 오류: {}", mall.getDisplayName(), e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        }
    }

    // product_num 유니크 제약 위반인지 판별
    private static boolean isDuplicateProductNumConstraint(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && (msg.contains("uq_product_product_num") || msg.contains("Duplicate entry") && msg.contains("product_num"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    //product_num 중복 시
    private ProductResDTO.ImportDTO handleDuplicateProductNum(Long userId, User user, Long productNum) {
        Product existing = productRepository.findByProductNum(productNum)
                .orElseThrow(() -> new ProductException(ProductErrorCode.CRAWLING_FAILED));
        productRepository.updateUpdatedAt(existing.getProductId());
        return resolveOrLinkUserProduct(userId, user, existing, true);
    }

    // 기존 Product에 대한 UserProduct 연결 또는 updatedAt 갱신 후 ImportDTO 반환 (PRODUCT202용)
    private ProductResDTO.ImportDTO resolveOrLinkUserProduct(Long userId, User user, Product product, boolean isUpdated) {
        UserProduct up = userProductRepository.findByUser_UserIdAndProduct_ProductId(userId, product.getProductId()).orElse(null);
        if (up == null) {
            UserProduct saved = userProductRepository.save(UserProduct.builder().user(user).product(product).build());
            return ProductConverter.toImportDTO(saved, isUpdated, false);
        }
        userProductRepository.updateUpdatedAt(userId, product.getProductId(), LocalDateTime.now());
        return ProductConverter.toImportDTO(up, isUpdated, false);
    }

    // 크롤링 데이터로 Product와 UserProduct 생성 후 ImportDTO 반환 (PRODUCT201용)
    private ProductResDTO.ImportDTO createProductAndLink(User user, ProductCrawlingData data) {
        Product product = Product.builder()
                .shoppingmallName(data.getShoppingmallName())
                .productUrl(data.getProductUrl())
                .category(data.getCategory())
                .productImgUrl(data.getProductImgUrl())
                .productName(data.getProductName())
                .brandName(data.getBrandName())
                .price(data.getPrice())
                .starPoint(data.getStarPoint())
                .aiReview(data.getAiReview())
                .productNum(data.getProductNum())
                .build();
        Product savedProduct = productRepository.save(product);
        UserProduct savedUp = userProductRepository.save(UserProduct.builder().user(user).product(savedProduct).build());
        return ProductConverter.toImportDTO(savedUp, false, false);
    }

    @Override
    public ProductResDTO.ImportDTO importMusinsaProduct(Long userId, ProductReqDTO.ImportMusinsaDTO dto) {
        return doImportByMall(userId, dto.getProduct_url(), ShoppingMall.MUSINSA);
    }

    @Override
    public ProductResDTO.LikeToggleDTO toggleProductLike(Long userId, Long productId) {
        // 상품 존재 여부 검증
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // User 조회 (매핑이 새로 생길 수도 있으므로)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.CRAWLING_FAILED));

        // 해당 사용자의 UserProduct 조회
        UserProduct userProduct = userProductRepository
                .findByUser_UserIdAndProduct_ProductId(userId, productId)
                .orElseGet(() -> {
                    // 매핑이 없으면 새로 생성 (기본 isLiked=false)
                    UserProduct up = UserProduct.builder()
                            .user(user)
                            .product(product)
                            .build();
                    return userProductRepository.save(up);
                });

        // 좋아요 상태 토글
        userProduct.toggleLike();

        // JPA 변경 감지로 업데이트, 혹시 모르니 save 호출
        UserProduct saved = userProductRepository.save(userProduct);

        return ProductResDTO.LikeToggleDTO.builder()
                .is_liked(saved.getIsLiked())
                .build();
    }
  
    // 쇼핑몰별 크롤링 공통 호출
    private ProductCrawlingData crawlProduct(String url, ShoppingMall mall) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("product_url", url);
            String responseJson = webClient.post()
                    .uri(fastApiBaseUrl + "/crawl/" + mall.getCrawlPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(CRAWL_TIMEOUT_SECONDS))
                    .block();
            log.debug("FastAPI 응답: {}", responseJson);
            JsonNode jsonNode = objectMapper.readTree(responseJson);
            return parseCrawlingResponse(jsonNode, url, mall.getDisplayName());
        } catch (WebClientResponseException e) {
            log.error("FastAPI 서버 호출 실패 (status: {}, body: {}): {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        } catch (Exception e) {
            log.error("FastAPI 서버 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        }
    }

    private ProductCrawlingData parseCrawlingResponse(JsonNode jsonNode, String requestUrl, String defaultMallName) {
        ProductCrawlingData data = new ProductCrawlingData();
        data.setShoppingmallName(jsonNode.has("shoppingmall_name") ? jsonNode.get("shoppingmall_name").asText() : defaultMallName);
        data.setProductUrl(jsonNode.has("product_url") ? jsonNode.get("product_url").asText() : requestUrl);
        data.setCategory(getTextOr(jsonNode, "category", "-"));
        data.setProductImgUrl(getTextOr(jsonNode, "product_img_url", "-"));
        data.setProductName(getTextOr(jsonNode, "product_name", "-"));
        data.setBrandName(getTextOr(jsonNode, "brand_name", "-"));
        data.setPrice(getTextOr(jsonNode, "price", "-"));
        data.setStarPoint(parseStarPoint(jsonNode));
        data.setAiReview(jsonNode.has("AI_review") && !jsonNode.get("AI_review").isNull() ? jsonNode.get("AI_review").asText() : null);
        data.setProductNum(parseProductNum(jsonNode));
        return data;
    }

    private static String getTextOr(JsonNode node, String key, String defaultValue) {
        return node.has(key) ? node.get(key).asText() : defaultValue;
    }

    private static Float parseStarPoint(JsonNode jsonNode) {
        if (!jsonNode.has("star_point") || jsonNode.get("star_point").isNull()) return null;
        JsonNode n = jsonNode.get("star_point");
        if (n.isNumber()) return (float) n.asDouble();
        if (n.isTextual()) {
            String s = n.asText();
            if ("-".equals(s)) return null;
            try { return Float.parseFloat(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private static Long parseProductNum(JsonNode jsonNode) {
        if (!jsonNode.has("product_num") || jsonNode.get("product_num").isNull()) return null;
        JsonNode n = jsonNode.get("product_num");
        if (n.isNumber()) return n.asLong();
        if (n.isTextual()) {
            try { return Long.parseLong(n.asText()); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    @Override
    public ProductResDTO.ImportDTO importZigzagProduct(Long userId, ProductReqDTO.ImportZigzagDTO dto) {
        return doImportByMall(userId, dto.getProduct_url(), ShoppingMall.ZIGZAG);
    }

    @Override
    public ProductResDTO.ImportDTO import29cmProduct(Long userId, ProductReqDTO.Import29cmDTO dto) {
        return doImportByMall(userId, dto.getProduct_url(), ShoppingMall.CM29);
    }

    @Override
    public ProductResDTO.ImportDTO importWconceptProduct(Long userId, ProductReqDTO.ImportWconceptDTO dto) {
        return doImportByMall(userId, dto.getProduct_url(), ShoppingMall.WCONCEPT);
    }

    // 크롤링 데이터를 담는 내부 클래스
    @Setter
    @Getter
    private static class ProductCrawlingData {
        private String shoppingmallName;
        private String productUrl;
        private String category;
        private String productImgUrl;
        private String productName;
        private String brandName;
        private String price;
        private Float starPoint;
        private String aiReview;
        private Long productNum;

    }
}
