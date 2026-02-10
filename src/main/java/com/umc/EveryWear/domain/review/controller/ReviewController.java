package com.umc.EveryWear.domain.review.controller;

import com.umc.EveryWear.domain.review.dto.req.ReviewReqDTO;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;
import com.umc.EveryWear.domain.review.exception.code.ReviewSuccessCode;
import com.umc.EveryWear.domain.review.service.command.ReviewCommandService;
import com.umc.EveryWear.domain.review.service.query.ReviewQueryService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import com.umc.EveryWear.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final ReviewQueryService reviewQueryService;

    @Operation(
            summary = "리뷰 크롤링 시작",
            description = """
                    상품 리뷰 크롤링을 시작합니다.
                    
                    **동작 방식:**
                    1. DB에 리뷰가 이미 있으면 즉시 반환 (캐시된 데이터)
                    2. 리뷰가 없으면 백그라운드에서 크롤링 시작
                    3. 크롤링이 진행 중이면 상태만 반환
                    
                    **응답 status:**
                    - `completed` → 즉시 리뷰 데이터 반환 (캐시)
                    - `processing` → 크롤링 진행 중 (약 30초 소요)
                    
                    **크롤링 진행 확인:**
                    - `GET /api/review/{productId}` API로 크롤링 상태 확인 가능
                    
                    **지원 쇼핑몰:**
                    - 무신사, 지그재그, 29CM, W컨셉
                    """
    )
    @PostMapping("/crawl")
    public ApiResponse<ReviewResDTO.ReviewListDTO> crawlReview(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReviewReqDTO.CrawlReviewDTO dto
    ) {
        ReviewResDTO.ReviewListDTO response = reviewCommandService.startReviewCrawling(dto);

        BaseSuccessCode successCode;
        if ("completed".equals(response.getStatus())) {
            successCode = ReviewSuccessCode.REVIEW_FOUND;
        } else if ("processing".equals(response.getStatus())) {
            successCode = ReviewSuccessCode.REVIEW_CRAWL_STARTED;
        } else {
            successCode = ReviewSuccessCode.REVIEW_ALREADY_CRAWLING;
        }

        return ApiResponse.onSuccess(successCode, response);
    }

    @Operation(
            summary = "리뷰 조회",
            description = """
                    상품의 리뷰를 조회합니다.
                    
                    **응답 status:**
                    - `completed`: 리뷰 데이터 있음 (크롤링 완료)
                    - `processing`: 크롤링 진행 중 (리뷰 데이터 없음)
                    - `failed`: 크롤링 실패
                    - `pending`: 크롤링 아직 시작 안 함
                    
                    **사용 시나리오:**
                    1. `POST /api/review/crawl`로 크롤링 시작
                    2. 이 API로 크롤링 상태 확인
                    3. `status: completed`가 되면 리뷰 데이터 사용
                    
                    **Polling 권장 주기:**
                    - 5초마다 확인 (최대 1분)
                    """,
            parameters = {
                    @Parameter(name = "productId", description = "상품 ID", required = true)
            }
    )
    @GetMapping("/{productId}")
    public ApiResponse<ReviewResDTO.ReviewListDTO> getReviews(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId
    ) {
        ReviewResDTO.ReviewListDTO response = reviewQueryService.getReviews(productId);

        BaseSuccessCode successCode = switch (response.getStatus()) {
            case "completed" -> ReviewSuccessCode.REVIEW_FOUND;
            case "processing" -> ReviewSuccessCode.REVIEW_CRAWLING;
            case "failed" -> ReviewSuccessCode.REVIEW_FAILED;
            default -> ReviewSuccessCode.REVIEW_NOT_STARTED;
        };

        return ApiResponse.onSuccess(successCode, response);
    }


    @Operation(
            summary = "특정 상품의 AI 리뷰 및 키워드 생성",
            description = "특정 상품의 모든 리뷰를 ChatGPT로 요약하고 키워드 4개를 추출합니다."
    )
    @PostMapping("/ai/{productId}")
    public ApiResponse<ReviewResDTO.AiReviewDTO> generateAiReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId
    ) {
        ReviewResDTO.AiReviewDTO aiResult = reviewCommandService.generateAiReview(productId);
        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, aiResult);
    }

    @Operation(
            summary = "AI 리뷰 조회",
            description = "특정 상품에 대해 이미 생성된 AI 요약과 키워드를 조회합니다."
    )
    @GetMapping("/ai/{productId}")
    public ApiResponse<ReviewResDTO.AiReviewDTO> getAiReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId
    ) {
        ReviewResDTO.AiReviewDTO response = reviewQueryService.getAiReview(productId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}