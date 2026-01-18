package com.umc.EveryWear.domain.review.controller;

import com.umc.EveryWear.domain.review.dto.AiReviewResult;
import com.umc.EveryWear.domain.review.service.command.ReviewCommandService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCommandService reviewCommandService;

    @Operation(
            summary = "특정 상품의 AI 리뷰 및 키워드 생성",
            description = "특정 상품의 모든 리뷰를 ChatGPT로 요약하고 키워드 4개를 추출합니다."
    )
    @PostMapping("/ai/{productId}")
    public ApiResponse<Map<String, Object>> generateAiReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId
    ) {
        AiReviewResult aiResult = reviewCommandService.generateAiReview(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("aiReview", aiResult.getSummary());
        result.put("keywords", aiResult.getKeywords());
        result.put("message", "AI 리뷰 및 키워드가 성공적으로 생성되었습니다.");

        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

}