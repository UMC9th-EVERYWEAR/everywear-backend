package com.umc.EveryWear.domain.fitting.controller;

import com.umc.EveryWear.domain.fitting.dto.req.FittingRequestDto;
import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.exception.code.FittingSuccessCode;
import com.umc.EveryWear.domain.fitting.service.command.FittingCommandService;
import com.umc.EveryWear.domain.fitting.service.query.FittingQueryService;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fittings")
@RequiredArgsConstructor
public class FittingController {

    private final FittingCommandService fittingCommandService;
    private final FittingQueryService fittingQueryService;

    /**
     * 가상 피팅 API
     */
    @Operation(
            summary = "가상 피팅 by 임준서(개발 완료)",
            description = "사용자의 대표사진에 상품 사진을 피팅합니다"
    )
    @PostMapping("/try-on")
    public ApiResponse<FittingResponseDto.FittingApplyResult> requestFitting(
            @AuthenticationPrincipal Long userId,
            @RequestBody FittingRequestDto.FittingRequest request
    ) {
        FittingResponseDto.FittingApplyResult result = fittingCommandService.requestFitting(
                userId,
                request.productId()
        );

        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_IMAGE_GENERATED,
                result
        );
    }

    /**
     * 내 피팅 목록 조회
     */
    @Operation(
            summary = "피팅 목록 조회 by 임준서(개발 완료)",
            description = "사용자의 피팅 목록을 조회합니다."
    )
    @GetMapping
    public ApiResponse<List<FittingResponseDto.FittingSummary>> getMyFittings(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_LIST_FETCHED,
                fittingQueryService.getMyFittings(userId)
        );
    }

    /**
     * 피팅 상세 조회
     */
    @Operation(
            summary = "피팅 상세 조회 by 임준서(개발 완료)",
            description = "피팅 내역 상세 정보를 조회합니다."
    )
    @GetMapping("/{fittingId}")
    public ApiResponse<FittingResponseDto.FittingDetail> getFittingDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long fittingId
    ) {
        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_DETAIL_FETCHED,
                fittingQueryService.getFittingDetail(userId, fittingId)
        );
    }

    @Operation(
            summary = "상품별 최근 피팅 조회",
            description = "특정 상품(productId)에 대해 현재 사용자(userId)의 가장 최근 피팅 1건을 조회합니다."
    )
    @GetMapping("/latest")
    public ApiResponse<FittingResponseDto.FittingApplyResult> getLatestFittingByProduct(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long productId
    ) {
        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_LATEST_FETCHED,
                fittingQueryService.getLatestFittingByProduct(userId, productId)
        );
    }
}