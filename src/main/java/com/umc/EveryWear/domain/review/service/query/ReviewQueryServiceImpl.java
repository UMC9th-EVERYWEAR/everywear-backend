package com.umc.EveryWear.domain.review.service.query;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.entity.ReviewKeyword;
import com.umc.EveryWear.domain.product.exception.ProductException;
import com.umc.EveryWear.domain.product.exception.code.ProductErrorCode;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.product.repository.ReviewKeywordRepository;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;
import com.umc.EveryWear.domain.review.entity.Review;
import com.umc.EveryWear.domain.review.exception.ReviewException;
import com.umc.EveryWear.domain.review.exception.code.ReviewErrorCode;
import com.umc.EveryWear.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;

    @Override
    public ReviewResDTO.ReviewListDTO getReviews(Long productId) {
        // Product 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PRODUCT_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByProduct_ProductId(productId);

        String status;
        if (!reviews.isEmpty()) {
            status = "completed";
        } else if (product.getReviewCrawlStatus() == null) {
            status = "not_started";
        } else {
            status = product.getReviewCrawlStatus().name().toLowerCase();
        }

        List<ReviewResDTO.ReviewDTO> reviewDTOs = reviews.stream()
                .map(ReviewResDTO.ReviewDTO::from)
                .toList();

        return ReviewResDTO.ReviewListDTO.builder()
                .status(status)
                .total_count(reviews.size())
                .reviews(reviewDTOs)
                .build();
    }

    @Override
    public ReviewResDTO.AiReviewDTO getAiReview(Long productId) {
        // 1. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 2. 키워드 조회 (ReviewKeyword 엔티티 -> String 리스트 변환)
        List<String> keywords = reviewKeywordRepository.findByProduct(product).stream()
                .map(ReviewKeyword::getKeywordName)
                .toList();

        // 3. DTO 반환
        return ReviewResDTO.AiReviewDTO.builder()
                .summary(product.getAiReview()) // 상품 테이블에 저장된 요약
                .keywords(keywords)             // 키워드 테이블에서 가져온 목록
                .message("AI 리뷰 조회 성공")
                .build();
    }
}