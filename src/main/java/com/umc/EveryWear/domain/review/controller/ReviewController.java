package com.umc.EveryWear.domain.review.controller;

import com.umc.EveryWear.domain.review.dto.req.ReviewReqDTO;
import com.umc.EveryWear.domain.review.dto.res.ReviewResDTO;
import com.umc.EveryWear.domain.review.exception.code.ReviewSuccessCode;
import com.umc.EveryWear.domain.review.service.command.ReviewCommandService;
import com.umc.EveryWear.domain.review.service.query.ReviewQueryService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.BaseSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @Operation(
            summary = "리뷰 크롤링 시작",
            description = "상품 리뷰 크롤링을 시작합니다. DB에 리뷰가 있으면 즉시 반환하고, 없으면 크롤링을 시작합니다."
    )
    @PostMapping("/crawl")
    public ApiResponse<ReviewResDTO.CrawlResponseDTO> crawlReview(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReviewReqDTO.CrawlReviewDTO dto
    ) {
        ReviewResDTO.CrawlResponseDTO response = reviewCommandService.startReviewCrawling(dto);

        BaseSuccessCode successCode;
        if (response.getFrom_cache() != null && response.getFrom_cache()) {
            successCode = ReviewSuccessCode.REVIEW_FOUND;
        } else if ("processing".equals(response.getStatus()) && response.getEstimated_time() != null) {
            successCode = ReviewSuccessCode.REVIEW_CRAWL_STARTED;
        } else {
            successCode = ReviewSuccessCode.REVIEW_ALREADY_CRAWLING;
        }

        return ApiResponse.onSuccess(successCode, response);
    }

    @Operation(
            summary = "리뷰 조회",
            description = "상품의 리뷰를 조회합니다. 크롤링 상태와 함께 반환됩니다."
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
}