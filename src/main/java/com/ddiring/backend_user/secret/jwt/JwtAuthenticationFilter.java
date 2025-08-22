package com.ddiring.backend_user.secret.jwt;

import com.ddiring.backend_user.entity.User;
import com.ddiring.backend_user.redis.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if ("/api/user/logout".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token) && !redisService.isRemoveToken(token)) {
            String role = jwtTokenProvider.getRole(token);
            String authority = switch (role) {
                case "ADMIN" -> "ROLE_ADMIN";
                case "CREATOR" -> "ROLE_CREATOR";
                case "USER" -> "ROLE_USER";
                default -> "ROLE_GUEST";
            };

            String principal = "admin";
            if (!User.Role.ADMIN.name().equals(role)) {
                principal = jwtTokenProvider.getUserSeqFromSubject(token);
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
