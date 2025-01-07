package com.raffleease.raffleease.Domains.Token.Services.Impls;

import com.raffleease.raffleease.Domains.Token.Services.IBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class BlackListServiceImpl implements IBlackListService {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addTokenToBlackList(String tokenId, Long expiration) {
        redisTemplate.opsForValue().set(tokenId, "blacklisted", expiration, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isTokenBlackListed(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenId));
    }
}
