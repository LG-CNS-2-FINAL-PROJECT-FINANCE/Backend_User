package com.ddiring.backend_user.config;

import com.ddiring.backend_user.redis.RedisService;
import com.ddiring.backend_user.secret.jwt.JwtAuthenticationFilter;
import com.ddiring.backend_user.secret.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("*")); // TODO: 프론트 주소 변경
                    config.setAllowedMethods(List.of("GET", "POST"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/auth/signup",
                                "/api/user/auth/login",
                                "/api/user/auth/admin/signup",
                                "/api/user/auth/admin/login"
                        ).permitAll()
                        .anyRequest().authenticated())
                // JwtAuthenticationFilter 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, redisService),
                        BasicAuthenticationFilter.class
                );

        return http.build();
    }
}
