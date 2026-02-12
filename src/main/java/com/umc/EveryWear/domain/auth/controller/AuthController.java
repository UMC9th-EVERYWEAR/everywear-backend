package com.umc.EveryWear.domain.auth.controller;

import com.umc.EveryWear.domain.auth.dto.TokenRefreshResponse;
import com.umc.EveryWear.domain.auth.service.AuthService;
import com.umc.EveryWear.global.apiPayload.ApiResponse;
import com.umc.EveryWear.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "AccessToken 재발급", description = "쿠키의 refreshToken으로 accessToken을 재발급합니다.")
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookieValue(request, "refreshToken");
        TokenRefreshResponse result = authService.refresh(refreshToken, response);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @Operation(summary = "AccessToken 재발급 (스웨거 테스트용)", description = "리프레시 토큰을 직접 파라미터로 넣어 엑세스 토큰을 재발급받습니다.")
    @PostMapping("/refresh/test")
    public ApiResponse<TokenRefreshResponse> refreshTest(
            @RequestParam("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        // 공통 로직 호출
        TokenRefreshResponse result = authService.processRefresh(refreshToken, response);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }


    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 RefreshToken을 무효화합니다.")
    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response
    ) {
        authService.logout(userId, response);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "로그아웃이 완료되었습니다.");
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 삭제하고 카카오 연결을 끊습니다.")
    @DeleteMapping("/withdraw")
    public ApiResponse<String> withdraw(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response
    ) {
        authService.withdraw(userId, response);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "회원 탈퇴가 완료되었습니다.");
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}