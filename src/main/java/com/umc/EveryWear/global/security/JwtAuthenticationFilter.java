package com.umc.EveryWear.global.security;

import com.umc.EveryWear.domain.user.entity.User;
import com.umc.EveryWear.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // JWT 검증을 건너뛸 경로들
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/oauth2/",
            "/login/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/oauth/callback"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDE_URLS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                Long userId = jwtUtil.getUserIdFromToken(jwt);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // RefreshToken이 없거나 빈 문자열이면 로그아웃된 상태로 간주합니다.
                if (user.getRefreshToken() == null || user.getRefreshToken().trim().isEmpty()) {
                    log.warn("이미 로그아웃된 사용자입니다. (Access Token 유효하지만 Refresh Token 없음) - User: {}", user.getEmail());

                    // 인증 설정 없이 필터 체인 진행 -> Spring Security가 403 Forbidden 처리함
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, null);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Set authentication for user: {}", user.getEmail());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}