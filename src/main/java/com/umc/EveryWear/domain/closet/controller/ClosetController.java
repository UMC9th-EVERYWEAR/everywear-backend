package com.umc.EveryWear.domain.closet.controller;

import com.umc.EveryWear.domain.closet.dto.res.ClosetResDTO;
import com.umc.EveryWear.domain.closet.exception.code.ClosetSuccessCode;
import com.umc.EveryWear.domain.closet.service.query.ClosetQueryService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Closet", description = "옷장 관련 API")
@RestController
@RequestMapping("/api/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClosetQueryService closetQueryService;

    @Operation(
            summary = "내 옷장 상품 조회",
            description = "내 옷장 상품을 최신순으로 조회합니다."
    )
    @GetMapping
    public ApiResponse<ClosetResDTO.ProductListResponse> getClosetProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ClosetResDTO.ProductListResponse response = closetQueryService.getClosetProducts(userId);
        return ApiResponse.onSuccess(ClosetSuccessCode.CLOSET_PRODUCTS_RETRIEVED, response);
    }

    @Operation(
            summary = "내 옷장 상의 상품 조회",
            description = "내 옷장 상의 상품을 최신순으로 조회합니다."
    )
    @GetMapping("/top")
    public ApiResponse<ClosetResDTO.ProductListResponse> getClosetTopProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ClosetResDTO.ProductListResponse response = closetQueryService.getClosetTopProducts(userId);
        return ApiResponse.onSuccess(ClosetSuccessCode.CLOSET_TOP_PRODUCTS_RETRIEVED, response);
    }

    @Operation(
            summary = "내 옷장 하의 상품 조회",
            description = "내 옷장 하의 상품을 최신순으로 조회합니다."
    )
    @GetMapping("/bottom")
    public ApiResponse<ClosetResDTO.ProductListResponse> getClosetBottomProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ClosetResDTO.ProductListResponse response = closetQueryService.getClosetBottomProducts(userId);
        return ApiResponse.onSuccess(ClosetSuccessCode.CLOSET_BOTTOM_PRODUCTS_RETRIEVED, response);
    }

    @Operation(
            summary = "내 옷장 아우터 상품 조회",
            description = "내 옷장 아우터 상품을 최신순으로 조회합니다."
    )
    @GetMapping("/outer")
    public ApiResponse<ClosetResDTO.ProductListResponse> getClosetOuterProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ClosetResDTO.ProductListResponse response = closetQueryService.getClosetOuterProducts(userId);
        return ApiResponse.onSuccess(ClosetSuccessCode.CLOSET_OUTER_PRODUCTS_RETRIEVED, response);
    }

    @Operation(
            summary = "내 옷장 원피스 상품 조회",
            description = "내 옷장 원피스 상품을 최신순으로 조회합니다."
    )
    @GetMapping("/dress")
    public ApiResponse<ClosetResDTO.ProductListResponse> getClosetDressProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ClosetResDTO.ProductListResponse response = closetQueryService.getClosetDressProducts(userId);
        return ApiResponse.onSuccess(ClosetSuccessCode.CLOSET_DRESS_PRODUCTS_RETRIEVED, response);
    }

    @Operation(
            summary = "내 옷장 기타 상품 조회",
            description = "is_liked가 true인 기타(category=기타) 상품 전체를 최신 업데이트 순(update_at 내림차순)으로 조회합니다. 등록한 상품이 없으면 products는 null입니다."
    )
    @GetMapping("/etc")
    public ApiResponse<ClosetResDTO.ProductListResponse> getClosetEtcProducts(
            @AuthenticationPrincipal Long userId
    ) {
        ClosetResDTO.ProductListResponse response = closetQueryService.getClosetEtcProducts(userId);
        return ApiResponse.onSuccess(ClosetSuccessCode.CLOSET_ETC_PRODUCTS_RETRIEVED, response);
    }
}
