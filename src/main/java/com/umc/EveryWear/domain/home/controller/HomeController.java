package com.umc.EveryWear.domain.home.controller;

import com.umc.EveryWear.domain.fitting.dto.res.FittingResponseDto;
import com.umc.EveryWear.domain.home.dto.res.HomeResDTO;
import com.umc.EveryWear.domain.home.exception.code.HomeSuccessCode;
import com.umc.EveryWear.domain.home.service.query.HomeQueryService;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Home", description = "홈 화면 관련 API")
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeQueryService homeQueryService;

    @Operation(
            summary = "홈 화면 상품 조회",
            description = "사용자가 등록한 전체 상품 중 최신 업데이트 순으로 상위 6개 상품을 조회합니다."
    )
    @GetMapping("/products")
    public ApiResponse<HomeResDTO.ProductListResponse> getHomeProducts(
            @AuthenticationPrincipal Long userId
    ) {
        HomeResDTO.ProductListResponse response = homeQueryService.getHomeProducts(userId);
        return ApiResponse.onSuccess(HomeSuccessCode.HOME_PRODUCTS_RETRIEVED, response);
    }

    @Operation(
            summary = "홈 화면 최근 피팅내역 조회 by 임준서(개발 완료)",
            description = "사용자의 피팅 내역 중 최신순 6개를 조회합니다."
    )
    @GetMapping("/recent-fittings")
    public ApiResponse<List<FittingResponseDto.FittingSummary>> getRecentFittings(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                HomeSuccessCode.RECENT_FITTINGS_FETCHED,
                homeQueryService.getRecentFittings(userId)
        );
    }
}
