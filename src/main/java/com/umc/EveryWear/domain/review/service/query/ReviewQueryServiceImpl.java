package com.umc.EveryWear.domain.review.service.query;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.entity.ReviewKeyword;
import com.umc.EveryWear.domain.product.enums.ReviewCrawlStatus;
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
        // 1. Product 조회 (상태값을 확인하기 위해 필요)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.PRODUCT_NOT_FOUND));

        ReviewCrawlStatus statusEnum = product.getReviewCrawlStatus();

        // 상태 문자열 결정 (null이거나 PENDING이면 "pending"으로 표시)
        String statusStr = (statusEnum != null) ? statusEnum.name().toLowerCase() : "pending";

        List<ReviewResDTO.ReviewDTO> reviewDTOs;

        // 2. 로직 수정: 오직 'COMPLETED' 상태일 때만 리뷰 데이터를 조회함
        if (ReviewCrawlStatus.COMPLETED.equals(statusEnum)) {
            List<Review> reviews = reviewRepository.findByProduct_ProductId(productId);
            reviewDTOs = reviews.stream()
                    .map(ReviewResDTO.ReviewDTO::from)
                    .toList();
        } else {
            // 3. 그 외의 상태(pending, processing, failed)라면 DB 조회 없이 바로 빈 리스트 반환
            reviewDTOs = List.of();
        }

        return ReviewResDTO.ReviewListDTO.builder()
                .status(statusStr)
                .total_count(reviewDTOs.size())
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