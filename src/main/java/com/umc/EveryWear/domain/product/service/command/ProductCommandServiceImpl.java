package com.umc.EveryWear.domain.product.service.command;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;

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

    // 무신사 상품 정보 크롤링
    // 일단 Python 코드를 참고하여 Java로 구현
    private ProductCrawlingData crawlMusinsaProduct(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        ProductCrawlingData data = new ProductCrawlingData();
        data.setShoppingmallName("무신사");
        data.setProductUrl(url);

        // 카테고리 추출
        String category = extractCategory(doc);
        data.setCategory(category);

        // 대표 이미지 추출
        String imageUrl = extractProductImage(doc);
        data.setProductImgUrl(imageUrl != null ? imageUrl : "-");

        // 상품명 추출
        String productName = extractProductName(doc);
        data.setProductName(productName != null ? productName : "-");

        // 브랜드명 추출
        String brandName = extractBrandName(doc);
        data.setBrandName(brandName != null ? brandName : "-");

        // 가격 추출
        String price = extractPrice(doc);
        data.setPrice(price != null ? price : "-");

        // 별점 추출
        Float starPoint = extractStarPoint(doc);
        data.setStarPoint(starPoint != null ? starPoint : 0.0f);

        // AI 리뷰
        data.setAiReview(null);

        return data;
    }

    private String extractCategory(Document doc) {
        try {
            Elements categoryElements = doc.select("[data-category-name]");
            if (categoryElements.isEmpty()) {
                return "기타";
            }

            String selectedCategory = null;
            String[] priorityCategories = {"아우터", "바지", "상의", "원피스/스커트"};

            for (Element element : categoryElements) {
                String categoryName = element.attr("data-category-name");
                if (categoryName != null && !categoryName.isEmpty()) {
                    for (String priority : priorityCategories) {
                        if (categoryName.contains(priority)) {
                            selectedCategory = categoryName;
                            break;
                        }
                    }
                    if (selectedCategory != null) break;
                }
            }

            if (selectedCategory == null && !categoryElements.isEmpty()) {
                selectedCategory = categoryElements.first().attr("data-category-name");
            }

            // 카테고리 매핑
            if (selectedCategory != null) {
                if (selectedCategory.contains("아우터")) {
                    return "아우터";
                } else if (selectedCategory.contains("바지")) {
                    return "하의";
                } else if (selectedCategory.contains("상의")) {
                    return "상의";
                } else if (selectedCategory.contains("원피스") || selectedCategory.contains("스커트")) {
                    return "원피스";
                }
            }

            return "기타";
        } catch (Exception e) {
            log.warn("카테고리 추출 실패: {}", e.getMessage());
            return "기타";
        }
    }

    private String extractProductImage(Document doc) {
        try {
            // 대표 이미지 선택자 (Python 코드의 XPath를 CSS 선택자로 변환)
            Element imgElement = doc.select("#root > div:first-child > div:first-child > div:first-child > div:first-child > div:first-child > div > div:first-child > img").first();
            if (imgElement != null) {
                return imgElement.attr("src");
            }
            // 대체 선택자
            imgElement = doc.select("img[src*='msscdn.net']").first();
            if (imgElement != null) {
                return imgElement.attr("src");
            }
        } catch (Exception e) {
            log.warn("이미지 URL 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractProductName(Document doc) {
        try {
            Elements elements = doc.select("span.text-title_18px_med.font-pretendard[data-mds='Typography']");
            if (!elements.isEmpty()) {
                return elements.last().text().trim();
            }
        } catch (Exception e) {
            log.warn("상품명 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractBrandName(Document doc) {
        try {
            Element brandElement = doc.select("#root > div:first-child > div:first-child > div:nth-child(5) > div:nth-child(2) > div > div:first-child > div > span").first();
            if (brandElement != null) {
                return brandElement.text().trim();
            }
            // 대체 선택자
            brandElement = doc.select("span:contains(브랜드)").first();
            if (brandElement != null) {
                return brandElement.nextElementSibling() != null ? brandElement.nextElementSibling().text().trim() : null;
            }
        } catch (Exception e) {
            log.warn("브랜드명 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractPrice(Document doc) {
        try {
            Elements elements = doc.select("span.text-title_18px_semi.font-pretendard[data-mds='Typography']");
            if (!elements.isEmpty()) {
                String priceText = elements.last().text().trim();
                // 가격 형식 검증 (숫자와 원 포함)
                if (priceText.matches(".*[0-9,]+원.*")) {
                    return priceText;
                }
            }
        } catch (Exception e) {
            log.warn("가격 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private Float extractStarPoint(Document doc) {
        try {
            Elements elements = doc.select("span.text-body_13px_med.font-pretendard[data-mds='Typography']");
            for (Element element : elements) {
                String text = element.text().trim();
                // 숫자와 소수점만으로 구성된 텍스트 찾기
                Pattern pattern = Pattern.compile("^\\d+\\.?\\d*$");
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    try {
                        float value = Float.parseFloat(text);
                        if (value >= 0 && value <= 5) {
                            return value;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("별점 추출 실패: {}", e.getMessage());
        }
        return null;
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
