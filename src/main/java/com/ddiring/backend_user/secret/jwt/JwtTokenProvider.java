package com.ddiring.backend_user.secret.jwt;

import com.ddiring.backend_user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import javax.crypto.SecretKey;
import java.util.Map;

@Component
public class JwtTokenProvider {

        private final SecretKey key;
        private final long expireIn = 1000L * 60 * 60 * 24; // 1일
        private final long refreshExpireIn = 1000L * 60 * 60 * 24 * 7; // 7일
        private static final String AUTHORITIES_KEY = "role";

        public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
                byte[] keyBytes = Decoders.BASE64.decode(secretKey);
                this.key = Keys.hmacShaKeyFor(keyBytes);
        }

        public String createToken(User user) {
                String role = (user.getRole() != null) ? user.getRole().name() : User.Role.GUEST.name();
                Claims claims = Jwts.claims()
                                .subject(String.valueOf(user.getUserSeq()))
                                .add("role", role)
                                .add("userSeq", user.getUserSeq())
                                .build();

                Date now = new Date();
                Date expiry = new Date(now.getTime() + expireIn);

                return Jwts.builder()
                                .claims(Map.of(
                                                Claims.SUBJECT, String.valueOf(user.getUserSeq()),
                                                "tokenType", "access",
                                                "role", role,
                                                "userSeq", user.getUserSeq()))
                                .issuedAt(now)
                                .expiration(expiry)
                                .signWith(key)
                                .compact();
        }

        public String adminCreateToken(User user) {
                String role = (user.getRole() != null) ? user.getRole().name() : User.Role.ADMIN.name();
                Claims claims = Jwts.claims()
                                .subject(user.getAdminId())
                                .add("role", role)
                                .add("userSeq", user.getUserSeq())
                                .build();

                Date now = new Date();
                Date expiry = new Date(now.getTime() + expireIn);

                return Jwts.builder()
                                .claims(Map.of(
                                                Claims.SUBJECT, user.getAdminId(),
                                                "tokenType", "access",
                                                "role", role,
                                                "userSeq", user.getUserSeq()))
                                .issuedAt(now)
                                .expiration(expiry)
                                .signWith(key)
                                .compact();
        }

        public boolean validateToken(String token) {
                try {
                        Jwts.parser()
                                        .verifyWith(key)
                                        .build()
                                        .parseSignedClaims(token);
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public String getUserSeqFromSubject(String token) {
                return Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .getSubject();
        }

        public long getRemainingTime(String token) {
                Date expiration = Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .getExpiration();

                long now = System.currentTimeMillis();
                return (expiration.getTime() - now) / 1000;
        }

        public String getRole(String token) {
                return Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .get("role", String.class);
        }

        public String createRefreshToken(User user) {
                String role = (user.getRole() != null) ? user.getRole().name() : User.Role.GUEST.name();
                Claims claims = Jwts.claims()
                                .subject(String.valueOf(user.getUserSeq()))
                                .add("role", role)
                                .add("userSeq", user.getUserSeq())
                                .build();

                Date now = new Date();
                Date expiry = new Date(now.getTime() + refreshExpireIn);

                return Jwts.builder()
                                .claims(Map.of(
                                                Claims.SUBJECT, String.valueOf(user.getUserSeq()),
                                                "role", role,
                                                "userSeq", user.getUserSeq()))
                                .issuedAt(now)
                                .expiration(expiry)
                                .signWith(key)
                                .compact();
        }
}
