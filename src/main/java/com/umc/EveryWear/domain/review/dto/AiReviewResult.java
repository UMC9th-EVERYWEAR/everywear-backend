package com.umc.EveryWear.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 리뷰 생성 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReviewResult {

    /**
     * AI가 생성한 리뷰 요약
     */
    private String summary;

    /**
     * AI가 추출한 키워드 4개
     */
    private List<String> keywords;
}