package com.umc.EveryWear.domain.fitting.controller;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.fitting.exception.code.FittingSuccessCode;
import com.umc.EveryWear.domain.fitting.service.command.FittingCommandService;
import com.umc.EveryWear.domain.fitting.service.query.FittingQueryService;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fitting")
@RequiredArgsConstructor
public class FittingController {

    private final FittingCommandService fittingCommandService;
    private final FittingQueryService fittingQueryService;

    /**
     * 가상 피팅 API
     */
    @PostMapping("/try-on")
    public ApiResponse<FittingResponseDto.FittingApplyResult> tryOn(
            @RequestParam("productId") Long productId,
            @RequestParam("userImageId") Long userImageId,
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_PROCESS_COMPLETED,
                fittingCommandService.tryOn(
                        user,
                        productId,
                        userImageId
                )
        );
    }

    /**
     * 내 피팅 목록 조회
     */
    @GetMapping
    public ApiResponse<List<FittingResponseDto.FittingSummary>> getMyFittings(
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_LIST_FETCHED,
                fittingQueryService.getMyFittings(user)
        );
    }

    /**
     * 피팅 상세 조회
     */
    @GetMapping("/{fittingId}")
    public ApiResponse<FittingResponseDto.FittingDetail> getFittingDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long fittingId
    ) {
        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_DETAIL_FETCHED,
                fittingQueryService.getFittingDetail(user, fittingId)
        );
    }
}

    /**
     * 피팅 좋아요 토글
     */
    @PostMapping("/{fittingId}/like")
    public ApiResponse<Void> toggleLike(
            @AuthenticationPrincipal User user,
            @PathVariable Long fittingId
    ) {
        fittingCommandService.toggleLike(user, fittingId);
        return ApiResponse.onSuccess(
                FittingSuccessCode.FITTING_LIKE_TOGGLED,
                null);
    }

    /**
     * 좋아요한 피팅 목록 조회
     */
    @GetMapping("/likes")
    public ApiResponse<List<FittingResponseDto.FittingSummary>> getLikedFittings(
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.onSuccess(
                FittingSuccessCode.LIKED_FITTING_LIST_FETCHED,
                fittingQueryService.getLikedFittings(user)
        );
    }
}