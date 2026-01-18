package com.umc.EveryWear.domain.fitting.dto.res;

import java.time.LocalDateTime;

public class FittingResponseDto {

    /**
     * 피팅 목록 / 좋아요 목록 공통 요약 DTO
     */
    public record FittingSummary(
            Long fittingId,
            String fittingResultImage,
            Boolean isLiked,
            LocalDateTime createdAt
    ) {}

    /**
     * 피팅 상세 조회 DTO
     */
    public record FittingDetail(
            Long fittingId,
            String fittingResultImage,
            Boolean isLiked,
            String productName,
            String productCategory,
            LocalDateTime createdAt
    ) {}

    /**
     * 피팅 적용 결과 DTO
     */
    public record FittingApplyResult(
            Long fittingId,
            String fittingResultImage
    ) {}
}
