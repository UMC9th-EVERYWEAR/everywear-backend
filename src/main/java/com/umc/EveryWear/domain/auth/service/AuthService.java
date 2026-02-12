package com.umc.EveryWear.domain.auth.service;

import com.umc.EveryWear.domain.auth.dto.TokenRefreshResponse;
import com.umc.EveryWear.domain.fitting.repository.FittingHistoryRepository;
import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.entity.mapping.UserProduct;
import com.umc.EveryWear.domain.user.enums.UserStatus;
import com.umc.EveryWear.domain.user.repository.UserImgRepository;
import com.umc.EveryWear.domain.user.repository.UserProductRepository;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import com.umc.EveryWear.global.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserImgRepository userImgRepository;
    private final UserProductRepository userProductRepository;
    private final FittingHistoryRepository fittingHistoryRepository;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    /**
     * [공통 로직] refreshToken 문자열을 받아 검증 후 새 토큰들 발급
     */
    @Transactional
    public TokenRefreshResponse processRefresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is missing.");
        }

        // 1) JWT 서명/만료 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        // 2) 토큰에서 userId 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 3) DB 조회 + 저장된 refreshToken과 일치하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh token does not match.");
        }

        // 4) 새 토큰 발급
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        // 5) DB 업데이트
        user.updateRefreshToken(newRefreshToken);

        // 6) 쿠키 업데이트 (테스트용 API 호출 시에도 브라우저 쿠키를 동기화해줌)
        setRefreshTokenCookie(response, newRefreshToken);

        return new TokenRefreshResponse(newAccessToken);
    }

    /**
     * 기존 쿠키 방식 (프론트 운영용)
     */
    @Transactional
    public TokenRefreshResponse refresh(String refreshToken, HttpServletResponse response) {
        return processRefresh(refreshToken, response);
    }

    /**
     * 로그아웃
     * - RefreshToken 무효화
     * - 쿠키 삭제
     */
    @Transactional
    public void logout(Long userId, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // RefreshToken 무효화
        user.updateRefreshToken("");

        // 쿠키 삭제
        clearRefreshTokenCookie(response);

        log.info("User {} logged out successfully", user.getEmail());
    }

    /**
     * 회원 탈퇴
     * - 카카오 연결 끊기
     * - 사용자 상태를 DELETED로 변경 (또는 실제 삭제)
     * - 쿠키 삭제
     */
    @Transactional
    public void withdraw(Long userId, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        try {
            // 소셜 타입에 따라 연결 끊기
            switch (user.getSocialType()) {
                case KAKAO:
                    unlinkKakao(user.getOauthId());
                    log.info("Kakao unlink successful for user: {}", user.getEmail());
                    break;
                case GOOGLE:
                    log.info("Google user withdrawal - no API call needed");
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to unlink {} account for user: {}",
                    user.getSocialType(), user.getEmail(), e);
        }

        // 1. UserImg 삭제
        userImgRepository.deleteAllByUser_UserId(userId);

        // 2. FittingHistory 삭제 (UserProduct를 통해 연결됨)
        List<UserProduct> userProducts = userProductRepository.findAllByUser_UserId(userId);
        for (UserProduct up : userProducts) {
            fittingHistoryRepository.deleteAllByUserProduct(up);
        }

        // 3. UserProduct 삭제
        userProductRepository.deleteAllByUser_UserId(userId);

        // 4. user 삭제
        userRepository.delete(user);

        // 쿠키 삭제
        clearRefreshTokenCookie(response);

        log.info("User {} withdrew successfully (hard deleted)", user.getEmail());
    }

        /**
         * 카카오 연결 끊기 (회원 탈퇴 시)
         */
    private void unlinkKakao(String oauthId) {
        String url = "https://kapi.kakao.com/v1/user/unlink";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("target_id_type", "user_id");
        params.add("target_id", oauthId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, request, Map.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                log.info("Kakao unlink API response: {}", responseEntity.getBody());
            }
        } catch (Exception e) {
            log.error("Kakao unlink API failed", e);
            throw new RuntimeException("Failed to unlink Kakao account");
        }
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬이면 false, 운영(https)이면 true 추천
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 삭제
        response.addCookie(cookie);
    }
}