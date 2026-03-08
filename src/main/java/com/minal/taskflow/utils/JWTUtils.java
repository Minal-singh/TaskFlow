package com.minal.taskflow.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JWTUtils {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value(("${security.jwt.expiration-time}"))
    private int expirationTime;

    private SecretKey getSigningKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userName) {
        log.debug("Generating JWT token for user: {}", userName);
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("Role", "USER");
        return createToken(claims, userName);
    }

    public String createToken(Map<String, Object> claims, String userName) {
        log.debug("Creating JWT token with claims for user: {}", userName);
        String token = Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
        log.debug("JWT token created successfully for user: {}", userName);
        return token;
    }

    public Date extractExpiration(String token) {
        log.debug("Extracting expiration from token");
        return extractAllClaims(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        if (expired) {
            log.warn("Token is expired");
        }
        return expired;
    }

    public Boolean validateToken(String token) {
        try {
            boolean valid = !isTokenExpired(token);
            if (valid) {
                log.debug("Token validation successful");
            }
            return valid;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserName(String token) {
        log.debug("Extracting username from token");
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        try {
            log.debug("Extracting all claims from token");
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            throw e;
        }
    }
}
