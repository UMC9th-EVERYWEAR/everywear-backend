package com.umc.EveryWear.domain.review.service.command;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OpenAiService openAiService;

    /**
     * 특정 상품의 리뷰를 AI로 요약하여 Product.aiReview에 저장
     * @param productId 상품 ID
     * @return 생성된 AI 요약 리뷰
     */
    @Override
    public String generateAiReview(Long productId) {
        // 1. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));

        // 2. 해당 상품의 모든 리뷰 내용 조회
        List<String> reviewContents = reviewRepository.findReviewContentsByProductId(productId);

        if (reviewContents.isEmpty()) {
            log.info("상품 ID {}에 대한 리뷰가 없습니다.", productId);
            return null;
        }

        log.info("상품 ID {}의 리뷰 {}개를 AI로 요약 중...", productId, reviewContents.size());

        // 3. OpenAI API를 통해 리뷰 요약
        String aiReview = openAiService.summarizeReviews(reviewContents);

        // 4. Product의 aiReview 필드 업데이트
        productRepository.updateAiReview(product.getProductId(), aiReview);

        log.info("상품 ID {}의 AI 리뷰 생성 완료", productId);

        return aiReview;
    }
}