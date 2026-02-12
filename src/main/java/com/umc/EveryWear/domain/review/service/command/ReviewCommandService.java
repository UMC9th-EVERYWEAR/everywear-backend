package com.umc.EveryWear.domain.review.service.command;

import com.umc.EveryWear.domain.review.dto.req.ReviewReqDTO;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;

public interface ReviewCommandService {
    ReviewResDTO.ReviewListDTO startReviewCrawling(ReviewReqDTO.CrawlReviewDTO dto);

    /**
     * 특정 상품의 AI 리뷰 및 키워드 생성
     * @param productId 상품 ID
     * @return 생성된 AI 요약 리뷰 및 키워드
     */
    ReviewResDTO.AiReviewDTO generateAiReview(Long productId);
}