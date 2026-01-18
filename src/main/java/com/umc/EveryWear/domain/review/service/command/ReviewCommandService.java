package com.umc.EveryWear.domain.review.service.command;

public interface ReviewCommandService {

    /**
     * 특정 상품의 AI 리뷰 생성
     * @param productId 상품 ID
     * @return 생성된 AI 요약 리뷰
     */
    String generateAiReview(Long productId);
}