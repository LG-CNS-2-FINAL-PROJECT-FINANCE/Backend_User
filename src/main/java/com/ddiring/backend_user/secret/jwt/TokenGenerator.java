package com.ddiring.backend_user.secret.jwt;

import com.ddiring.backend_user.secret.jwt.dto.TokenDto;
import com.ddiring.backend_user.secret.jwt.props.JwtConfigProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private final JwtConfigProperties configProperties;

    private volatile SecretKey secretKey;

    private SecretKey getSecretKey() {
        if (secretKey == null) {
            synchronized (this) {
                if (secretKey == null) {
                    secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(configProperties.getSecretKey()));
                }
            }
        }

        return secretKey;
    }

    public TokenDto.AccessToken generateAccessToken(String userId, String deviceType) {
        TokenDto.JwtToken jwtToken = this.generateJwtToken(userId, deviceType, false);
        return new TokenDto.AccessToken(jwtToken);
    }

    public TokenDto.AccessRefreshToken generateAccessRefreshToken(String userId, String deviceType) {
        TokenDto.JwtToken accessJwtToken = this.generateJwtToken(userId, deviceType, false);
        TokenDto.JwtToken refreshJwtToken = this.generateJwtToken(userId, deviceType, true);
        return new TokenDto.AccessRefreshToken(accessJwtToken, refreshJwtToken);
    }

    private TokenDto.JwtToken generateJwtToken(String userId, String deviceType, boolean refreshToken) {
        int tokenExpiresIn = tokenExpireIn(refreshToken, deviceType);
        String tokenType = refreshToken ? "refresh" : "access";

        String token =
                        Jwts
                        .builder()
                        .issuer("welab")
                        .subject(userId)
                        .claim("userId", userId)
                        .claim("deviceType", deviceType)
                        .claim("tokenType", tokenType)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + tokenExpiresIn * 1000L))
                        .signWith(getSecretKey())
                        .header()
                        .add("typ", "JWT")
                        .and()
                        .compact();

        return new TokenDto.JwtToken(token, tokenExpiresIn);
    }

    private int tokenExpireIn(boolean refreshToken, String deviceType) {
        if (!refreshToken) {
            return 60 * 15;
        }

        if ("MOBILE".equals(deviceType)) {
            return configProperties.getMobileExpiresIn();
        } else if ("TABLET".equals(deviceType)) {
            return configProperties.getTabletExpiresIn();
        } else {
            return configProperties.getExpiresIn();
        }
    }
}
