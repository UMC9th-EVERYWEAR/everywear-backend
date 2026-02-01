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
        Long fittingId = fittingCommandService.requestFitting(
                userId,
                request.productId()
        );

        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_IMAGE_GENERATED,
                new FittingResponseDto.FittingApplyResult(fittingId)
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
}