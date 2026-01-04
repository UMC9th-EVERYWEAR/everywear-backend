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

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);   // 로컬은 false, HTTPS 운영은 true
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        response.addCookie(refreshCookie);


        // 테스트용: 토큰을 단순 HTML 페이지로 표시
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(
                "<html><body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                        "<h2 style='color: #4CAF50;'>✅ 로그인 성공!</h2>" +
                        "<div style='margin: 20px 0;'>" +
                        "<p><strong>사용자 정보:</strong></p>" +
                        "<ul>" +
                        "<li>이름: " + savedUser.getName() + "</li>" +
                        "<li>이메일: " + (savedUser.getEmail() != null ? savedUser.getEmail() : "제공되지 않음") + "</li>" +
                        "</ul>" +
                        "</div>" +
                        "<div style='margin: 20px 0;'>" +
                        "<p><strong>Access Token:</strong></p>" +
                        "<textarea readonly style='width:100%; height:100px; font-family: monospace;'>" + accessToken + "</textarea>" +
                        "</div>" +
                        "<div style='margin: 20px 0;'>" +
                        "<p><strong>Refresh Token:</strong></p>" +
                        "<textarea readonly style='width:100%; height:100px; font-family: monospace;'>" + refreshToken + "</textarea>" +
                        "</div>" +
                        "<hr>" +
                        "<div style='background: #f5f5f5; padding: 15px; border-radius: 5px;'>" +
                        "<p><strong>API 테스트 방법:</strong></p>" +
                        "<pre style='background: #333; color: #fff; padding: 10px; border-radius: 3px; overflow-x: auto;'>" +
                        "curl -X GET http://localhost:8080/api/user/me \\\n" +
                        "  -H \"Authorization: Bearer " + accessToken + "\"" +
                        "</pre>" +
                        "</div>" +
                        "</body></html>"
        );

        // 프론트엔드가 준비되면 아래 코드로 변경
        /*
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
        */
    }
}