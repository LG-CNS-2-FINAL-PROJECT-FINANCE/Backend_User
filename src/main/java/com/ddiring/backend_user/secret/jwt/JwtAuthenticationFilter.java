package com.ddiring.backend_user.secret.jwt;

import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/auth/login".equals(path) || "/auth/register".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        String token = null;
        // 요청 헤더에서 JWT 토큰 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        // 추출한 토큰 유효 시 SecurityContextHolder에 인증 정보 설정
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                // 토큰 유효성 검증 및 Authentication 객체 생성
                org.springframework.security.core.Authentication authentication = jwtTokenProvider
                        .getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.error("설정 중 오류 발생",e);
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("토큰 유효하지 않음");
        }
        filterChain.doFilter(request, response);
    }
}