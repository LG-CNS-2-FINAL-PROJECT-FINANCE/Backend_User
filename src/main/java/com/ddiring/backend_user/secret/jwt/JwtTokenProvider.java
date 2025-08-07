package com.ddiring.backend_user.secret.jwt;

import com.ddiring.backend_user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final long expireIn = 1000 * 60 * 60;

    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getKakaoId()).build();
        claims.put("role", user.getRole().name());
        claims.put("nickname", user.getNickname());
        claims.put("userSeq", user.getUserSeq());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireIn);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("role", role)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getKakaoId(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
