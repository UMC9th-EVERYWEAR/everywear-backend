package com.umc.EveryWear.domain.fitting.dto.res;

import java.time.LocalDateTime;

public class FittingResponseDto {

    /**
     * 피팅 목록 요약 DTO
     */
    public record FittingSummary(
            Long fittingId,
            String fittingResultImage,
            LocalDateTime createdAt,
            ProductBrief product
    ) {}

    /**
     * 피팅 목록 요약 DTO에 필요한 상품 정보
     */
    public record ProductBrief(
            Long productId,
            String productName,
            Boolean isLiked
    ) {}

    /**
     * 피팅 상세 조회 DTO
     */
    public record FittingDetail(
            Long fittingId,

            // BEFORE / AFTER
            String beforeImageUrl,   // 유저 대표 이미지
            String afterImageUrl,    // 피팅 결과 이미지

            LocalDateTime createdAt,

            // 상품 카드 정보
            ProductSummary product
    ) {}

    /**
     * 피팅 상세 조회에서 필요한 상품 정보
     */
    public record ProductSummary(
            Long productId,
            String siteName,
            String productName,
            String price,
            Float rating,
            String purchaseUrl,
            Boolean isLiked
    ) {}

    /**
     * 피팅 적용 결과 DTO
     */
    public record FittingApplyResult(
            Long fittingId,
            String fittingResultImageUrl
    ) {}
}
