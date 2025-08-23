package com.ddiring.backend_user.config;

import com.ddiring.backend_user.redis.RedisService;
import com.ddiring.backend_user.secret.jwt.JwtAuthenticationFilter;
import com.ddiring.backend_user.secret.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/user/auth/login",
                                                                "/api/user/auth/admin/login",
                                                                "/api/user/auth/admin/signup",
                                                                "/api/user/logout",
                                                                "/api/user/detail"
                                                )
                                                .permitAll()
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/user/info")
                                                .hasAnyRole("USER", "CREATOR", "GUEST")
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/user/list",
                                                                "/api/user/search",
                                                                "/api/user/info")
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisService),
                                                UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}
