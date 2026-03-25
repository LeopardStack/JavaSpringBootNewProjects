package com.scnu.springbootjdk17demo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generate(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String parseUsername(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public boolean validate(String token) {
        try {
            parseUsername(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}