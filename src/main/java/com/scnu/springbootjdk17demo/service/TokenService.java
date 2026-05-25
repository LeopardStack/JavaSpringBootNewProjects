package com.scnu.springbootjdk17demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 管理 token 在 Redis 里的存活状态。
 * <p>
 * Key 格式：auth:token:{jwt}
 * Value：username
 * TTL：跟 JWT 过期时间一致
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String KEY_PREFIX = "auth:token:";

    private final StringRedisTemplate redis;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /** 登录后写入 */
    public void store(String token, String username) {
        redis.opsForValue().set(
                KEY_PREFIX + token,
                username,
                expirationMs,
                TimeUnit.MILLISECONDS
        );
    }

    /** 校验：返回 null 即视为已失效 */
    public String getUsername(String token) {
        return redis.opsForValue().get(KEY_PREFIX + token);
    }

    /** 主动登出 */
    public void revoke(String token) {
        redis.delete(KEY_PREFIX + token);
    }

    /** 滑动会话：每次访问续 TTL（可选） */
    public void refresh(String token) {
        redis.expire(KEY_PREFIX + token, expirationMs, TimeUnit.MILLISECONDS);
    }
}