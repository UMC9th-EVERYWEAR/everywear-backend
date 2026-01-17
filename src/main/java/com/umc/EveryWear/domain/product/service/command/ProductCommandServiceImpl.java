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
    public ProductResDTO.ImportDTO importMusinsaProduct(ProductReqDTO.ImportDTO dto) {
        try {
            // 2차 URL 형식 검증(이중 보호 처리)
            String productUrl = dto.getProduct_url();
            if (productUrl == null || !productUrl.matches("^https://www\\.musinsa\\.com/products/\\d+$")) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // 이미 등록된 상품인지 확인
            Product existingProduct = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProduct != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProduct.getProductId());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProduct.getProductId())
                        .orElse(existingProduct);
                return ProductConverter.toImportDTO(updatedProduct);
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

            return ProductConverter.toImportDTO(savedProduct);

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
                    .timeout(Duration.ofSeconds(120)) // 크롤링은 시간이 걸릴 수 있으므로 120초 타임아웃
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

    @Override
    public ProductResDTO.ImportDTO importZigzagProduct(ProductReqDTO.ImportZigzagDTO dto) {
        try {
            // 2차 URL 형식 검증(이중 보호 처리)
            String productUrl = dto.getProduct_url();
            if (productUrl == null || !productUrl.matches("^https://zigzag\\.kr/catalog/products/\\d+$")) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // 이미 등록된 상품인지 확인
            Product existingProduct = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProduct != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProduct.getProductId());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProduct.getProductId())
                        .orElse(existingProduct);
                return ProductConverter.toImportDTO(updatedProduct);
            }

            // 크롤링 수행
            ProductCrawlingData crawlerData = crawlZigzagProduct(dto.getProduct_url());

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

            return ProductConverter.toImportDTO(savedProduct);

        } catch (Exception e) {
            log.error("지그재그 상품 크롤링 중 오류 발생: {}", e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        }
    }

    // 지그재그 상품 정보 크롤링 - FastAPI 서버 호출
    private ProductCrawlingData crawlZigzagProduct(String url) {
        try {
            // FastAPI 서버에 요청
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("product_url", url);
            
            String responseJson = webClient.post()
                    .uri(fastApiBaseUrl + "/crawl/zigzag")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120)) // 크롤링은 시간이 걸릴 수 있으므로 120초 타임아웃
                    .block();
            
            log.debug("FastAPI 응답: {}", responseJson);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(responseJson);
            
            // ProductCrawlingData로 변환
            ProductCrawlingData data = new ProductCrawlingData();
            data.setShoppingmallName(jsonNode.has("shoppingmall_name") ? 
                    jsonNode.get("shoppingmall_name").asText() : "지그재그");
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
            
            // 별점 처리 (null 허용, 문자열 "-"도 null로 처리)
            if (jsonNode.has("star_point") && !jsonNode.get("star_point").isNull()) {
                JsonNode starPointNode = jsonNode.get("star_point");
                if (starPointNode.isNumber()) {
                    data.setStarPoint((float) starPointNode.asDouble());
                } else if (starPointNode.isTextual()) {
                    String starPointStr = starPointNode.asText();
                    if ("-".equals(starPointStr)) {
                        data.setStarPoint(null);
                    } else {
                        try {
                            data.setStarPoint(Float.parseFloat(starPointStr));
                        } catch (NumberFormatException e) {
                            data.setStarPoint(null);
                        }
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
  
  
    @Override
    public ProductResDTO.ImportDTO import29cmProduct(ProductReqDTO.Import29cmDTO dto) {
        try {
            // 2차 URL 형식 검증(이중 보호 처리)
            String productUrl = dto.getProduct_url();
            if (productUrl == null || !productUrl.matches("^(https://www\\.29cm\\.co\\.kr/products/\\d+.*|https://29cm\\.onelink\\.me/.*)$")) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // 이미 등록된 상품인지 확인
            Product existingProduct = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProduct != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProduct.getProductId());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProduct.getProductId())
                        .orElse(existingProduct);
                return ProductConverter.toImportDTO(updatedProduct);
            }

            // 크롤링 수행
            ProductCrawlingData crawlerData = crawl29cmProduct(dto.getProduct_url());

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

            return ProductConverter.toImportDTO(savedProduct);

        } catch (Exception e) {
            log.error("29cm 상품 크롤링 중 오류 발생: {}", e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        }
    }
              
    // 29cm 상품 정보 크롤링 - FastAPI 서버 호출
    private ProductCrawlingData crawl29cmProduct(String url) {
        try {
            // FastAPI 서버에 요청
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("product_url", url);
            
            String responseJson = webClient.post()
                    .uri(fastApiBaseUrl + "/crawl/29cm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120)) // 크롤링은 시간이 걸릴 수 있으므로 120초 타임아웃
                    .block();
            
            log.debug("FastAPI 응답: {}", responseJson);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(responseJson);
            
            // ProductCrawlingData로 변환
            ProductCrawlingData data = new ProductCrawlingData();
            data.setShoppingmallName(jsonNode.has("shoppingmall_name") ? 
                    jsonNode.get("shoppingmall_name").asText() : "29CM");
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


    @Override
    public ProductResDTO.ImportDTO importWconceptProduct(ProductReqDTO.WconceptImportDTO dto) {
        try {
            // 2차 URL 형식 검증(이중 보호 처리)
            String productUrl = dto.getProduct_url();
            if (productUrl == null || !productUrl.matches("^https://www\\.wconcept\\.co\\.kr/Product/\\d+\\?.*$")) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // 이미 등록된 상품인지 확인
            Product existingProduct = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProduct != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProduct.getProductId());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProduct.getProductId())
                        .orElse(existingProduct);
                return ProductConverter.toImportDTO(updatedProduct);
            }

            // 크롤링 수행
            ProductCrawlingData crawlerData = crawlWconceptProduct(dto.getProduct_url());

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

            return ProductConverter.toImportDTO(savedProduct);

        } catch (Exception e) {
            log.error("W컨셉 상품 크롤링 중 오류 발생: {}", e.getMessage(), e);
            throw new ProductException(ProductErrorCode.CRAWLING_FAILED);
        }
    }

    // W컨셉 상품 정보 크롤링 - FastAPI 서버 호출
    private ProductCrawlingData crawlWconceptProduct(String url) {
        try {
            // FastAPI 서버에 요청
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("product_url", url);
            
            String responseJson = webClient.post()
                    .uri(fastApiBaseUrl + "/crawl/wconcept")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120)) // 크롤링은 시간이 걸릴 수 있으므로 120초 타임아웃
                    .block();
            
            log.debug("FastAPI 응답: {}", responseJson);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(responseJson);
            
            // ProductCrawlingData로 변환
            ProductCrawlingData data = new ProductCrawlingData();
            data.setShoppingmallName(jsonNode.has("shoppingmall_name") ? 
                    jsonNode.get("shoppingmall_name").asText() : "W컨셉");
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
