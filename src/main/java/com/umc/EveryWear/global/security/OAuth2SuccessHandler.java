package com.umc.EveryWear.global.security;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import com.umc.EveryWear.domain.user.service.CustomOAuth2User;
import com.umc.EveryWear.global.util.CookieUtils;
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

import static com.umc.EveryWear.global.security.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

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

        // Refresh Token 업데이트
        User savedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        savedUser.updateRefreshToken(refreshToken);

        // 1. 쿠키에서 리다이렉트 대상 URL 가져오기
        String targetBaseUrl = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("https://www.everywear.cloud/login/callback"); // 쿠키 없으면 기본 배포 주소

        // 2. 응답 쿠키 설정 (RefreshToken)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(targetBaseUrl.startsWith("https"));
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        response.addCookie(refreshCookie);

        // 3. 최종 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString(targetBaseUrl)
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();

        // 4. 사용한 인증 쿠키 삭제
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}