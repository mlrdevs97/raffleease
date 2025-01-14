package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Token.Services.IBlackListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class BlackListServiceImpl implements IBlackListService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.application.config.is_test}")
    private boolean isTest;


    @Override
    public void addTokenToBlackList(String id, Long expiration) {
        if (isTest) log.info("Adding to blacklist. ID: {}, Expiration: {}ms", id, expiration);

        if (Objects.isNull(id) || id.trim().isEmpty()) {
            if (isTest) log.error("Invalid ID: {}", id);
            throw new IllegalArgumentException("Token ID must not be null or empty");
        }

        if (Objects.isNull(expiration) || expiration <= 0) {
            if (isTest) log.error("Invalid expiration time: {}", expiration);
            throw new IllegalArgumentException("Expiration must be a positive number");
        }

        try {
            redisTemplate.opsForValue().set(id, "blacklisted", expiration, TimeUnit.MILLISECONDS);
            if (isTest) log.info("Token successfully added to blacklist. Token ID: {}", id);
        } catch (Exception e) {
            if (isTest)
                log.error("Failed to add token to blacklist. Token ID: {}, Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to add token to blacklist", e);
        }
    }

    @Override
    public boolean isTokenBlackListed(String id) {
        if (isTest) log.info("Checking if token is blacklisted. Token ID: {}", id);

        if (Objects.isNull(id) || id.trim().isEmpty()) {
            if (isTest) log.error("Invalid ID: {}", id);
            throw new IllegalArgumentException("Token ID must not be null or empty");
        }

        boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(id));
        if (isTest && isBlacklisted) {
            log.info("Blacklisted. ID: {}", id);
        } else if (isTest) {
            log.info("Not blacklisted. ID: {}", id);
        }
        return isBlacklisted;
    }
}