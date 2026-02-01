package com.umc.EveryWear.domain.review.service.query;

import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;

public interface ReviewQueryService {
    ReviewResDTO.ReviewListDTO getReviews(Long productId);
    ReviewResDTO.AiReviewDTO getAiReview(Long productId);
}