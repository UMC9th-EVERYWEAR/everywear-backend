package com.umc.EveryWear.domain.review.service.command;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.enums.ReviewCrawlStatus;
import com.umc.EveryWear.domain.product.entity.ReviewKeyword;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.review.dto.req.ReviewReqDTO;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;
import com.umc.EveryWear.domain.review.entity.Review;
import com.umc.EveryWear.domain.review.exception.ReviewException;
import com.umc.EveryWear.domain.review.exception.code.ReviewErrorCode;
import com.umc.EveryWear.domain.product.repository.ReviewKeywordRepository;
import com.umc.EveryWear.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final WebClient webClient;

    @Value("${FASTAPI_BASE_URL}")
    private String fastApiBaseUrl;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final OpenAiService openAiService;

    @Override
    public ReviewResDTO.ReviewListDTO startReviewCrawling(ReviewReqDTO.CrawlReviewDTO dto) {
        Long productId = dto.getProduct_id();

        // 1. Product 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PRODUCT_NOT_FOUND));

        // 2. 리뷰 크롤링을 완료한적이 있는지 확인 (캐시 반환)
        if (product.getReviewCrawlStatus() == ReviewCrawlStatus.COMPLETED) {
            List<Review> reviews = reviewRepository.findByProduct_ProductId(productId);
            List<ReviewResDTO.ReviewDTO> reviewDTOs = reviews.stream()
                    .map(ReviewResDTO.ReviewDTO::from)
                    .toList();

            return ReviewResDTO.ReviewListDTO.builder()
                    .status("completed")
                    .total_count(reviews.size())
                    .reviews(reviewDTOs)
                    .build();
        }

        // 3. 이미 크롤링 중인지 확인
        if (product.getReviewCrawlStatus() == ReviewCrawlStatus.PROCESSING) {
            return ReviewResDTO.ReviewListDTO.builder()
                    .status("processing")
                    .total_count(0)
                    .reviews(List.of())
                    .build();
        }

        // 4. 크롤링 상태 업데이트
        product.updateReviewCrawlStatus(ReviewCrawlStatus.PROCESSING);

        // 5. FastAPI에 크롤링 요청 (Fire-and-Forget)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product_id", dto.getProduct_id());
        requestBody.put("product_url", dto.getProduct_url());
        requestBody.put("shoppingmall_name", dto.getShoppingmall_name());
        requestBody.put("review_count", 20);

        webClient.post()
                .uri(fastApiBaseUrl + "/crawler/review/crawl")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        result -> log.info("리뷰 크롤링 요청 성공: productId={}", productId),
                        error -> log.error("리뷰 크롤링 요청 실패: productId={}, error={}", productId, error.getMessage())
                );

        return ReviewResDTO.ReviewListDTO.builder()
                .status("processing")
                .total_count(0)
                .reviews(List.of())
                .build();
    }
    /**
     * 특정 상품의 리뷰를 AI로 요약하고 키워드를 추출하여 저장
     * @param productId 상품 ID
     * @return 생성된 AI 요약 리뷰 및 키워드
     */
    @Override
    public ReviewResDTO.AiReviewDTO generateAiReview(Long productId) {
        // 1. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));

        // 2. 해당 상품의 모든 리뷰 내용 조회
        List<String> reviewContents = reviewRepository.findReviewContentsByProductId(productId);

        if (reviewContents.isEmpty()) {
            log.info("상품 ID {}에 대한 리뷰가 없습니다.", productId);
            return ReviewResDTO.AiReviewDTO.builder()
                    .summary(null)
                    .keywords(List.of())
                    .build();
        }

        log.info("상품 ID {}의 리뷰 {}개를 AI로 요약 및 키워드 추출 중...", productId, reviewContents.size());

        // 3. OpenAI API를 통해 리뷰 요약 및 키워드 추출
        ReviewResDTO.AiReviewDTO aiResult = openAiService.summarizeReviewsWithKeywords(reviewContents);

        // 4. Product의 aiReview 필드 업데이트
        productRepository.updateAiReview(product.getProductId(), aiResult.getSummary());

        // 5. 기존 키워드 삭제
        reviewKeywordRepository.deleteByProductId(productId);

        // 6. 새로운 키워드 저장
        saveKeywords(product, aiResult.getKeywords());

        log.info("상품 ID {}의 AI 리뷰 및 키워드 생성 완료", productId);

        return aiResult;
    }


    /**
     * 키워드 저장
     */
    private void saveKeywords(Product product, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            log.warn("저장할 키워드가 없습니다. 상품 ID: {}", product.getProductId());
            return;
        }

        for (String keywordName : keywords) {
            if (keywordName != null && !keywordName.trim().isEmpty()) {
                ReviewKeyword keyword = ReviewKeyword.builder()
                        .product(product)
                        .keywordName(keywordName.trim())
                        .build();

                reviewKeywordRepository.save(keyword);
                log.debug("키워드 저장 완료: {} (상품 ID: {})", keywordName, product.getProductId());
            }
        }
    }

}