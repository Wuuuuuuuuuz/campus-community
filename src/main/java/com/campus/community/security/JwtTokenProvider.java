package com.campus.community.security;

import cn.hutool.core.util.IdUtil;
import com.campus.community.constant.RedisConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;
    private final long refreshExpiration;
    private final RedisTemplate<String, Object> redisTemplate;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration,
                            @Value("${jwt.refresh-expiration}") long refreshExpiration,
                            RedisTemplate<String, Object> redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
        this.redisTemplate = redisTemplate;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        String token = IdUtil.simpleUUID();
        String key = RedisConstants.REFRESH_TOKEN_PREFIX + token;
        String userKey = RedisConstants.REFRESH_TOKEN_USER_PREFIX + userId;
        redisTemplate.opsForValue().set(key, userId, refreshExpiration, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(userKey, token, refreshExpiration, TimeUnit.SECONDS);
        return token;
    }

    public String refreshAccessToken(String refreshToken) {
        String key = RedisConstants.REFRESH_TOKEN_PREFIX + refreshToken;
        Object userIdObj = redisTemplate.opsForValue().get(key);
        if (userIdObj == null) {
            return null;
        }
        return String.valueOf(userIdObj);
    }

    public void deleteRefreshToken(Long userId) {
        String userKey = RedisConstants.REFRESH_TOKEN_USER_PREFIX + userId;
        Object oldToken = redisTemplate.opsForValue().get(userKey);
        if (oldToken != null) {
            redisTemplate.delete(RedisConstants.REFRESH_TOKEN_PREFIX + oldToken);
            redisTemplate.delete(userKey);
        }
    }

    public void blacklistAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return;
        long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (remainingTime > 0) {
            String key = RedisConstants.TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "", remainingTime, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = RedisConstants.TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.debug("JWT parse error: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        return parseClaims(token) != null && !isTokenBlacklisted(token);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            return Long.valueOf(claims.getSubject());
        }
        return null;
    }
}
