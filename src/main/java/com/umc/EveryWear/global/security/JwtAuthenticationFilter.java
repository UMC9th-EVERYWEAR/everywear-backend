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
            "/oauth/callback",
            "/api/auth/",
            "/",
            "/health"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = EXCLUDE_URLS.stream().anyMatch(path::startsWith);

        // 디버깅 로그 (배포 후 확인용)
        if (!shouldSkip) {
            log.info("🔒 JWT Filter WILL RUN for path: {}", path);
        } else {
            log.info("⏭️  JWT Filter SKIPPED for path: {}", path);
        }

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("🔍 JWT Filter executing for: {}", request.getRequestURI());

        try {
            String jwt = getJwtFromRequest(request);

            // Authorization 헤더가 없으면 그냥 통과
            if (!StringUtils.hasText(jwt)) {
                log.info("No JWT token found, proceeding without authentication");
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtUtil.validateToken(jwt)) {
                Long userId = jwtUtil.getUserIdFromToken(jwt);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.getRefreshToken() == null || user.getRefreshToken().trim().isEmpty()) {
                    log.warn("이미 로그아웃된 사용자입니다. - User: {}", user.getEmail());
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