package com.umc.EveryWear.domain.review.service.query;

import com.umc.EveryWear.domain.product.entity.Product;
import com.umc.EveryWear.domain.product.entity.ReviewCrawlStatus;
import com.umc.EveryWear.domain.product.repository.ProductRepository;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;
import com.umc.EveryWear.domain.review.entity.Review;
import com.umc.EveryWear.domain.review.exception.ReviewException;
import com.umc.EveryWear.domain.review.exception.code.ReviewErrorCode;
import com.umc.EveryWear.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

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
}