package com.umc.EveryWear.domain.product.service.command;

import com.umc.EveryWear.domain.product.converter.ProductConverter;
import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.entity.ProductCrawlJob;
import com.umc.EveryWear.domain.product.enums.ProductCrawlStatus;
import com.umc.EveryWear.domain.product.enums.ShoppingMall;
import com.umc.EveryWear.domain.product.exception.ProductException;
import com.umc.EveryWear.domain.product.exception.code.ProductErrorCode;
import com.umc.EveryWear.domain.product.repository.ProductCrawlJobRepository;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    private final ProductCrawlJobRepository productCrawlJobRepository;
    private final WebClient webClient;

    @Value("${FASTAPI_BASE_URL}")
    private String fastApiBaseUrl;

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
        return doImportByMall(userId, productUrl, mall);
    }

    // 쇼핑몰 공통 import(캐시 → 진행 중 확인 → Fire-and-Forget)
    private ProductResDTO.ImportResult doImportByMall(Long userId, String productUrl, ShoppingMall mall) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.CRAWLING_FAILED));
        if (!mall.matchesUrl(productUrl)) {
            throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
        }

        // 이미 해당 URL로 등록된 상품이 있으면 캐시 반환
        Product existingByUrl = productRepository.findByProductUrl(productUrl).orElse(null);
        if (existingByUrl != null) {
            ProductResDTO.ImportDTO dto = resolveOrLinkUserProduct(userId, user, existingByUrl, false);
            return ProductResDTO.ImportResult.builder()
                    .status("completed")
                    .from_cache(true)
                    .product(dto)
                    .mall(mall)
                    .build();
        }

        // 같은 사용자·같은 URL로 크롤링 진행 중인 작업이 있으면 상태만 반환
        ProductCrawlJob inProgress = productCrawlJobRepository
                .findByUserIdAndProductUrlAndStatus(userId, productUrl, ProductCrawlStatus.PROCESSING)
                .orElse(null);
        if (inProgress != null) {
            return ProductResDTO.ImportResult.builder()
                    .status("processing")
                    .from_cache(false)
                    .job_id(inProgress.getJobId())
                    .estimated_time("60초")
                    .build();
        }

        // 크롤링 작업 생성 후 FastAPI에 Fire-and-Forget 요청
        ProductCrawlJob job = ProductCrawlJob.builder()
                .userId(userId)
                .productUrl(productUrl)
                .shoppingmallName(mall.getDisplayName())
                .status(ProductCrawlStatus.PROCESSING)
                .build();
        ProductCrawlJob savedJob = productCrawlJobRepository.save(job);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("job_id", savedJob.getJobId());
        requestBody.put("user_id", userId);
        requestBody.put("product_url", productUrl);
        requestBody.put("shoppingmall_name", mall.getDisplayName());

        webClient.post()
                .uri(fastApiBaseUrl + "/crawler/product/crawl")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        result -> log.info("상품 크롤링 요청 성공: jobId={}, productUrl={}", savedJob.getJobId(), productUrl),
                        error -> log.error("상품 크롤링 요청 실패: jobId={}, error={}", savedJob.getJobId(), error.getMessage())
                );

        return ProductResDTO.ImportResult.builder()
                .status("processing")
                .from_cache(false)
                .job_id(savedJob.getJobId())
                .estimated_time("60초")
                .build();
    }

    // 기존 Product에 대한 UserProduct 연결 또는 updatedAt 갱신 후 ImportDTO 반환
    private ProductResDTO.ImportDTO resolveOrLinkUserProduct(Long userId, User user, Product product, boolean isUrlUpdated) {
        UserProduct up = userProductRepository.findByUser_UserIdAndProduct_ProductId(userId, product.getProductId()).orElse(null);
        if (up == null) {
            UserProduct saved = userProductRepository.save(UserProduct.builder().user(user).product(product).build());
            return ProductConverter.toImportDTO(saved, true, isUrlUpdated);
        }
        userProductRepository.updateUpdatedAt(userId, product.getProductId(), LocalDateTime.now());
        return ProductConverter.toImportDTO(up, true, isUrlUpdated);
    }

    @Override
    public ProductResDTO.ImportResult importMusinsaProduct(Long userId, ProductReqDTO.ImportMusinsaDTO dto) {
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
  
    @Override
    public ProductResDTO.ImportResult importZigzagProduct(Long userId, ProductReqDTO.ImportZigzagDTO dto) {
        return doImportByMall(userId, dto.getProduct_url(), ShoppingMall.ZIGZAG);
    }

    @Override
    public ProductResDTO.ImportResult import29cmProduct(Long userId, ProductReqDTO.Import29cmDTO dto) {
        return doImportByMall(userId, dto.getProduct_url(), ShoppingMall.CM29);
    }

    @Override
    public ProductResDTO.ImportResult importWconceptProduct(Long userId, ProductReqDTO.ImportWconceptDTO dto) {
        return doImportByMall(userId, dto.getProduct_url(), ShoppingMall.WCONCEPT);
    }

    @Override
    public ProductResDTO.ImportResult getImportStatus(Long userId, Long jobId) {
        ProductCrawlJob job = productCrawlJobRepository.findById(jobId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (!job.getUserId().equals(userId)) {
            throw new ProductException(ProductErrorCode.PRODUCT_UNAUTHORIZED);
        }
        if (job.getStatus() == ProductCrawlStatus.PROCESSING) {
            return ProductResDTO.ImportResult.builder()
                    .status("processing")
                    .from_cache(false)
                    .job_id(job.getJobId())
                    .estimated_time("60초")
                    .build();
        }
        if (job.getStatus() == ProductCrawlStatus.FAILED) {
            return ProductResDTO.ImportResult.builder()
                    .status("failed")
                    .from_cache(false)
                    .build();
        }
        // COMPLETED: 상품·UserProduct 조회 후 dto 반환
        Product product = productRepository.findById(job.getProductId())
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
        UserProduct userProduct = userProductRepository
                .findByUser_UserIdAndProduct_ProductId(job.getUserId(), product.getProductId())
                .orElse(null);
        ProductResDTO.ImportDTO dto = userProduct != null
                ? ProductConverter.toImportDTO(userProduct)
                : ProductConverter.toImportDTO(product);
        ShoppingMall mall = ShoppingMall.fromDisplayName(job.getShoppingmallName());
        return ProductResDTO.ImportResult.builder()
                .status("completed")
                .from_cache(false)
                .product(dto)
                .mall(mall)
                .build();
    }
}
