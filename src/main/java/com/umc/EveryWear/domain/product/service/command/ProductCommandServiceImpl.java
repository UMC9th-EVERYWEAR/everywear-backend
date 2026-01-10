package com.umc.EveryWear.domain.product.service.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.EveryWear.domain.product.converter.ProductConverter;
import com.umc.EveryWear.domain.product.dto.req.ProductReqDTO;
import com.umc.EveryWear.domain.product.dto.res.ProductResDTO;
import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.exception.ProductException;
import com.umc.EveryWear.domain.product.exception.code.ProductErrorCode;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;
    
    @Value("${fastapi.base-url:http://localhost:8001}")
    private String fastApiBaseUrl;

    @Override
    public ProductResDTO.CrawlingDTO crawlAndSaveMusinsaProduct(ProductReqDTO.CrawlingDTO dto) {
        try {
            // 이미 등록된 상품인지 확인
            Product existingProduct = productRepository.findByProductUrl(dto.getProduct_url())
                    .orElse(null);
            
            if (existingProduct != null) {
                return ProductConverter.toCrawlingDTO(existingProduct);
            }

            // 크롤링 수행
            ProductCrawlingData crawlerData = crawlMusinsaProduct(dto.getProduct_url());

            // Product 엔터티 생성 및 저장
            Product product = Product.builder()
                    .shoppingmallName(crawlerData.getShoppingmallName())
                    .productUrl(crawlerData.getProductUrl())
                    .category(crawlerData.getCategory())
                    .productImgUrl(crawlerData.getProductImgUrl())
                    .productName(crawlerData.getProductName())
                    .brandName(crawlerData.getBrandName())
                    .price(crawlerData.getPrice())
                    .starPoint(crawlerData.getStarPoint())
                    .aiReview(crawlerData.getAiReview())
                    .build();

            Product savedProduct = productRepository.save(product);

            return ProductConverter.toCrawlingDTO(savedProduct);

        } catch (Exception e) {
            log.error("무신사 상품 크롤링 중 오류 발생: {}", e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        }
    }

    // 무신사 상품 정보 크롤링 - FastAPI 서버 호출
    private ProductCrawlingData crawlMusinsaProduct(String url) {
        try {
            // FastAPI 서버에 요청
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("product_url", url);
            
            String responseJson = webClient.post()
                    .uri(fastApiBaseUrl + "/crawl/musinsa")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60)) // 크롤링은 시간이 걸릴 수 있으므로 60초 타임아웃
                    .block();
            
            log.debug("FastAPI 응답: {}", responseJson);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(responseJson);
            
            // ProductCrawlingData로 변환
            ProductCrawlingData data = new ProductCrawlingData();
            data.setShoppingmallName(jsonNode.has("shoppingmall_name") ? 
                    jsonNode.get("shoppingmall_name").asText() : "무신사");
            data.setProductUrl(jsonNode.has("product_url") ? 
                    jsonNode.get("product_url").asText() : url);
            data.setCategory(jsonNode.has("category") ? 
                    jsonNode.get("category").asText() : "-");
            data.setProductImgUrl(jsonNode.has("product_img_url") ? 
                    jsonNode.get("product_img_url").asText() : "-");
            data.setProductName(jsonNode.has("product_name") ? 
                    jsonNode.get("product_name").asText() : "-");
            data.setBrandName(jsonNode.has("brand_name") ? 
                    jsonNode.get("brand_name").asText() : "-");
            data.setPrice(jsonNode.has("price") ? 
                    jsonNode.get("price").asText() : "-");
            
            // 별점 처리 (null 허용)
            if (jsonNode.has("star_point") && !jsonNode.get("star_point").isNull()) {
                JsonNode starPointNode = jsonNode.get("star_point");
                if (starPointNode.isNumber()) {
                    data.setStarPoint((float) starPointNode.asDouble());
                } else if (starPointNode.isTextual()) {
                    try {
                        data.setStarPoint(Float.parseFloat(starPointNode.asText()));
                    } catch (NumberFormatException e) {
                        data.setStarPoint(null);
                    }
                } else {
                    data.setStarPoint(null);
                }
            } else {
                data.setStarPoint(null);
            }
            
            data.setAiReview(jsonNode.has("AI_review") && !jsonNode.get("AI_review").isNull() ? 
                    jsonNode.get("AI_review").asText() : null);
            
            return data;
            
        } catch (WebClientResponseException e) {
            log.error("FastAPI 서버 호출 실패 (status: {}, body: {}): {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        } catch (Exception e) {
            log.error("FastAPI 서버 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        }
    }


    // 크롤링 데이터를 담는 내부 클래스
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

        // Getters and Setters
        public String getShoppingmallName() { return shoppingmallName; }
        public void setShoppingmallName(String shoppingmallName) { this.shoppingmallName = shoppingmallName; }
        public String getProductUrl() { return productUrl; }
        public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getProductImgUrl() { return productImgUrl; }
        public void setProductImgUrl(String productImgUrl) { this.productImgUrl = productImgUrl; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public Float getStarPoint() { return starPoint; }
        public void setStarPoint(Float starPoint) { this.starPoint = starPoint; }
        public String getAiReview() { return aiReview; }
        public void setAiReview(String aiReview) { this.aiReview = aiReview; }
    }
}
