package com.umc.EveryWear.domain.fitting.dto.internal;

/**
 * Gemini 이미지 검증 결과 (내부 DTO)
 * - 외부 API(Gemini) 응답을 매핑하기 위한 용도
 * - Controller / Swagger / API 계약에 노출되지 않음
 */
public record VerificationResult(
        boolean isSuitable,
        double confidenceScore,
        String errorCode,
        String reason
) {}