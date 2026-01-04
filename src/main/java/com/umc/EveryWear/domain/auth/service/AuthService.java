package com.umc.EveryWear.domain.auth.service;

import com.umc.EveryWear.domain.auth.dto.TokenRefreshResponse;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import com.umc.EveryWear.global.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * refreshToken(쿠키) -> 검증 -> accessToken 재발급
     * + refreshToken도 회전(rotate)해서 DB/쿠키 업데이트 (권장)
     */
    @Transactional
    public TokenRefreshResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is missing.");
        }

        // 1) JWT 서명/만료 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        // 2) 토큰에서 userId 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 3) DB 조회 + 저장된 refreshToken과 일치하는지 확인 (중요!)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            // 탈취/재사용 방어
            throw new IllegalArgumentException("Refresh token does not match.");
        }

        // 4) 새 토큰 발급
        String newAccessToken = jwtUtil.generateAccessToken(user);

        // (권장) refresh token 회전
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        user.updateRefreshToken(newRefreshToken); // Dirty checking으로 저장

        // 5) refreshToken 쿠키로 내려주기 (HttpOnly)
        setRefreshTokenCookie(response, newRefreshToken);

        return new TokenRefreshResponse(newAccessToken);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬이면 false, 운영(https)이면 true 추천
        cookie.setPath("/");
        // cookie.setDomain("your-domain.com"); // 운영 시 필요하면 설정
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14일 (원하면 yml 값으로 빼도 됨)
        response.addCookie(cookie);
    }
}
