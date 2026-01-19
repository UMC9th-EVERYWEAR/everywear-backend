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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;
    
    @Value("${fastapi.base-url:http://localhost:8001}")
    private String fastApiBaseUrl;

    @Override
    public ProductResDTO.ImportDTO importMusinsaProduct(ProductReqDTO.ImportMusinsaDTO dto) {
        try {
            // 2차 URL 형식 검증(이중 보호 처리)
            String productUrl = dto.getProduct_url();
            boolean isValidUrl = productUrl != null && (
                    productUrl.matches("^https://www\\.musinsa\\.com/products/\\d+$") ||
                    productUrl.matches("^https://musinsa\\.onelink\\.me/[^/]+/[^/]+.*$")
            );
            if (!isValidUrl) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // URL로 이미 등록된 상품인지 확인 -> 그러면 크롤링X
            Product existingProductByUrl = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProductByUrl != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProductByUrl.getProductId(), LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByUrl.getProductId())
                        .orElse(existingProductByUrl);
                return ProductConverter.toImportDTO(updatedProduct, true);
            }

            // 크롤링 실행
            ProductCrawlingData crawlerData = crawlMusinsaProduct(dto.getProduct_url());

            // 크롤링 후 상품 고유값으로 이미 등록된 상품인지 확인
            Product existingProductByNum = null;
            if (crawlerData.getProductNum() != null) {
                existingProductByNum = productRepository.findByProductNum(crawlerData.getProductNum())
                        .orElse(null);
            }
            
            if (existingProductByNum != null) {
                // 상품 고윳값으로 이미 등록된 상품이 있으면 상품URL을 현재 URL로 업데이트하고 updatedAt 갱신
                productRepository.updateProductUrlAndUpdatedAt(existingProductByNum.getProductId(), productUrl, LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByNum.getProductId())
                        .orElse(existingProductByNum);
                return ProductConverter.toImportDTO(updatedProduct, true, true);
            }

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
                    .productNum(crawlerData.getProductNum())
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
            
            // product_num 처리
            if (jsonNode.has("product_num") && !jsonNode.get("product_num").isNull()) {
                JsonNode productNumNode = jsonNode.get("product_num");
                if (productNumNode.isNumber()) {
                    data.setProductNum(productNumNode.asLong());
                } else if (productNumNode.isTextual()) {
                    try {
                        data.setProductNum(Long.parseLong(productNumNode.asText()));
                    } catch (NumberFormatException e) {
                        data.setProductNum(null);
                    }
                } else {
                    data.setProductNum(null);
                }
            } else {
                data.setProductNum(null);
            }
            
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
            boolean isValidUrl = productUrl != null && (
                    productUrl.matches("^https://zigzag\\.kr/catalog/products/\\d+$") ||
                    productUrl.matches("^https://s\\.zigzag\\.kr/[A-Za-z0-9]+$")
            );
            if (!isValidUrl) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // URL로 이미 등록된 상품인지 확인 -> 그러면 크롤링X
            Product existingProductByUrl = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProductByUrl != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProductByUrl.getProductId(), LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByUrl.getProductId())
                        .orElse(existingProductByUrl);
                return ProductConverter.toImportDTO(updatedProduct, true);
            }

            // 크롤링 실행
            ProductCrawlingData crawlerData = crawlZigzagProduct(dto.getProduct_url());

            // 크롤링 후 상품 고윳값으로 이미 등록된 상품인지 확인
            Product existingProductByNum = null;
            if (crawlerData.getProductNum() != null) {
                existingProductByNum = productRepository.findByProductNum(crawlerData.getProductNum())
                        .orElse(null);
            }
            
            if (existingProductByNum != null) {
                // 상품 고윳값으로 이미 등록된 상품이 있으면 상품URL을 현재 URL로 업데이트하고 updatedAt 갱신
                productRepository.updateProductUrlAndUpdatedAt(existingProductByNum.getProductId(), productUrl, LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByNum.getProductId())
                        .orElse(existingProductByNum);
                return ProductConverter.toImportDTO(updatedProduct, true, true);
            }

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
                    .productNum(crawlerData.getProductNum())
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
            
            // product_num 처리
            if (jsonNode.has("product_num") && !jsonNode.get("product_num").isNull()) {
                JsonNode productNumNode = jsonNode.get("product_num");
                if (productNumNode.isNumber()) {
                    data.setProductNum(productNumNode.asLong());
                } else if (productNumNode.isTextual()) {
                    try {
                        data.setProductNum(Long.parseLong(productNumNode.asText()));
                    } catch (NumberFormatException e) {
                        data.setProductNum(null);
                    }
                } else {
                    data.setProductNum(null);
                }
            } else {
                data.setProductNum(null);
            }
            
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
            boolean isValidUrl = productUrl != null && (
                    productUrl.matches("^https://www\\.29cm\\.co\\.kr/products/\\d+.*$") ||
                    productUrl.matches("^https://29cm\\.onelink\\.me/.*$")
            );
            if (!isValidUrl) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // URL로 이미 등록된 상품인지 확인 -> 그러면 크롤링X
            Product existingProductByUrl = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProductByUrl != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProductByUrl.getProductId(), LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByUrl.getProductId())
                        .orElse(existingProductByUrl);
                return ProductConverter.toImportDTO(updatedProduct, true);
            }

            // 크롤링 실행
            ProductCrawlingData crawlerData = crawl29cmProduct(dto.getProduct_url());

            // 크롤링 후 상품 고윳값으로 이미 등록된 상품인지 확인
            Product existingProductByNum = null;
            if (crawlerData.getProductNum() != null) {
                existingProductByNum = productRepository.findByProductNum(crawlerData.getProductNum())
                        .orElse(null);
            }
            
            if (existingProductByNum != null) {
                // 상품 고윳값으로 이미 등록된 상품이 있으면 상품URL을 현재 URL로 업데이트하고 updatedAt 갱신
                productRepository.updateProductUrlAndUpdatedAt(existingProductByNum.getProductId(), productUrl, LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByNum.getProductId())
                        .orElse(existingProductByNum);
                return ProductConverter.toImportDTO(updatedProduct, true, true);
            }

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
                    .productNum(crawlerData.getProductNum())
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
            
            // product_num 처리
            if (jsonNode.has("product_num") && !jsonNode.get("product_num").isNull()) {
                JsonNode productNumNode = jsonNode.get("product_num");
                if (productNumNode.isNumber()) {
                    data.setProductNum(productNumNode.asLong());
                } else if (productNumNode.isTextual()) {
                    try {
                        data.setProductNum(Long.parseLong(productNumNode.asText()));
                    } catch (NumberFormatException e) {
                        data.setProductNum(null);
                    }
                } else {
                    data.setProductNum(null);
                }
            } else {
                data.setProductNum(null);
            }
            
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
            boolean isValidUrl = productUrl != null && (
                    productUrl.matches("^https://www\\.wconcept\\.co\\.kr/Product/\\d+\\?.*$") ||
                    productUrl.matches("^https://m\\.wconcept\\.co\\.kr/Product/\\d+\\?.*$")
            );
            if (!isValidUrl) {
                throw new ProductException(ProductErrorCode.INVALID_URL_FORMAT);
            }
            
            // URL로 이미 등록된 상품인지 확인 -> 그러면 크롤링X
            Product existingProductByUrl = productRepository.findByProductUrl(productUrl)
                    .orElse(null);
            
            if (existingProductByUrl != null) {
                // 기존 상품이 있으면 updatedAt을 현재 시간으로 업데이트
                productRepository.updateUpdatedAt(existingProductByUrl.getProductId(), LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByUrl.getProductId())
                        .orElse(existingProductByUrl);
                return ProductConverter.toImportDTO(updatedProduct, true);
            }

            // 크롤링 실행
            ProductCrawlingData crawlerData = crawlWconceptProduct(dto.getProduct_url());

            // 크롤링 후 상품 고윳값으로 이미 등록된 상품인지 확인
            Product existingProductByNum = null;
            if (crawlerData.getProductNum() != null) {
                existingProductByNum = productRepository.findByProductNum(crawlerData.getProductNum())
                        .orElse(null);
            }
            
            if (existingProductByNum != null) {
                // 상품 고윳값으로 이미 등록된 상품이 있으면 상품URL을 현재 URL로 업데이트하고 updatedAt 갱신
                productRepository.updateProductUrlAndUpdatedAt(existingProductByNum.getProductId(), productUrl, LocalDateTime.now());
                // 업데이트 후 다시 조회하여 최신 정보 반환
                Product updatedProduct = productRepository.findById(existingProductByNum.getProductId())
                        .orElse(existingProductByNum);
                return ProductConverter.toImportDTO(updatedProduct, true, true);
            }

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
                    .productNum(crawlerData.getProductNum())
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
            
            // product_num 처리
            if (jsonNode.has("product_num") && !jsonNode.get("product_num").isNull()) {
                JsonNode productNumNode = jsonNode.get("product_num");
                if (productNumNode.isNumber()) {
                    data.setProductNum(productNumNode.asLong());
                } else if (productNumNode.isTextual()) {
                    try {
                        data.setProductNum(Long.parseLong(productNumNode.asText()));
                    } catch (NumberFormatException e) {
                        data.setProductNum(null);
                    }
                } else {
                    data.setProductNum(null);
                }
            } else {
                data.setProductNum(null);
            }
            
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
