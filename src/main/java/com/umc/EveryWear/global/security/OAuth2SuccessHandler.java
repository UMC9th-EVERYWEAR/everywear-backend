package com.umc.EveryWear.global.security;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import com.umc.EveryWear.domain.user.service.CustomOAuth2User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // DB에서 사용자를 다시 조회하여 Refresh Token 업데이트
        User savedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Refresh Token 업데이트 (Dirty Checking으로 자동 업데이트됨)
        savedUser.updateRefreshToken(refreshToken);

        // 1. 리다이렉트 대상 URL 결정 (동적 처리)
        String referer = request.getHeader("Referer");
        String targetBaseUrl = "https://www.everywear.cloud/login/callback";

        // 만약 프론트엔드 로컬(localhost:5173)에서 요청이 왔다면 대상 변경
        if (referer != null && referer.contains("localhost:5173")) {
            targetBaseUrl = "http://localhost:5173/login/callback";
        }

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        boolean isSecure = targetBaseUrl.startsWith("https");
        refreshCookie.setSecure(isSecure);

        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        response.addCookie(refreshCookie);

        String targetUrl = UriComponentsBuilder.fromUriString(targetBaseUrl)
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}