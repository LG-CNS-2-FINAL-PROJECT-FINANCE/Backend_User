package com.ddiring.backend_user.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRemoveToken(String token, long expireSeconds) {
        redisTemplate.opsForValue().set(token, "logout", expireSeconds, TimeUnit.SECONDS);
    }

    public boolean isRemoveToken(String token) {
        return redisTemplate.hasKey(token);
    }
}
