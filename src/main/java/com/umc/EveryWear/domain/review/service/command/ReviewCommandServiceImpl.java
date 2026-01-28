package com.umc.EveryWear.domain.review.service.command;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.entity.ReviewCrawlStatus;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.review.dto.req.ReviewReqDTO;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;
import com.umc.EveryWear.domain.review.entity.Review;
import com.umc.EveryWear.domain.review.exception.ReviewException;
import com.umc.EveryWear.domain.review.exception.code.ReviewErrorCode;
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

    @Value("${fastapi.base-url:http://localhost:8001}")
    private String fastApiBaseUrl;

    @Override
    public ReviewResDTO.CrawlResponseDTO startReviewCrawling(ReviewReqDTO.CrawlReviewDTO dto) {
        Long productId = dto.getProduct_id();

        // 1. Product 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PRODUCT_NOT_FOUND));

        // 2. 리뷰가 이미 존재하는지 확인
        if (reviewRepository.existsByProduct_ProductId(productId)) {
            List<Review> reviews = reviewRepository.findByProduct_ProductId(productId);
            List<ReviewResDTO.ReviewDTO> reviewDTOs = reviews.stream()
                    .map(ReviewResDTO.ReviewDTO::from)
                    .toList();

            return ReviewResDTO.CrawlResponseDTO.builder()
                    .from_cache(true)
                    .status("completed")
                    .total_count(reviews.size())
                    .reviews(reviewDTOs)
                    .build();
        }

        // 3. 이미 크롤링 중인지 확인
        if (product.getReviewCrawlStatus() == ReviewCrawlStatus.PROCESSING) {
            return ReviewResDTO.CrawlResponseDTO.builder()
                    .status("processing")
                    .from_cache(false)
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

        return ReviewResDTO.CrawlResponseDTO.builder()
                .status("processing")
                .estimated_time("30초")
                .from_cache(false)
                .total_count(0)
                .reviews(List.of())
                .build();
    }
}