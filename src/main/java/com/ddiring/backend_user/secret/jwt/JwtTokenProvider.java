package com.ddiring.backend_user.secret.jwt;

import com.ddiring.backend_user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import javax.crypto.SecretKey;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    private final long expireIn = 1000 * 60 * 60; // 1시간

    private static final String AUTHORITIES_KEY = "role";

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(User user) {
        Claims claims = Jwts.claims()
                .add("role", user.getRole().name())
                .add("nickname", user.getNickname())
                .add("userSeq", user.getUserSeq())
                .build();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireIn);

        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .add("role", user.getRole().name())
                .and()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    public String adminCreateToken(User user) {
        Claims claims = Jwts.claims()
                .subject(user.getAdminId())
                .add("role", user.getRole().name())
                .add("userSeq", user.getUserSeq())
                .build();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireIn);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
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

    public String getKakaoId(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<SimpleGrantedAuthority> authorities = claims.get(AUTHORITIES_KEY, String.class)
                .lines()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
