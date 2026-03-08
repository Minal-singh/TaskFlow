package com.minal.taskflow.services;

import com.minal.taskflow.utils.JWTUtils;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistService {
    private final RedisTemplate<String, String> redis;
    private final JWTUtils jwtUtils;

    public JwtBlacklistService(RedisTemplate<String, String> redis, JWTUtils jwtUtils) {
        this.redis = redis;
        this.jwtUtils = jwtUtils;
    }

    public void blacklistToken(String token) {
        Claims claims = jwtUtils.extractAllClaims(token);
        String jti = claims.get("jti", String.class);

        long currentTime = System.currentTimeMillis() / 1000;

        long expiryTime = claims.get("exp", Long.class);
        long ttlSeconds = expiryTime - currentTime;

        if (ttlSeconds > 0) {
            redis.opsForValue().set("blacklist:" + jti, "1", ttlSeconds, TimeUnit.SECONDS);
        }
    }


    public boolean isBlacklisted(String token) {
        Claims claims = jwtUtils.extractAllClaims(token);
        String jti = claims.get("jti", String.class);
        return redis.hasKey("blacklist:" + jti);
    }
}

