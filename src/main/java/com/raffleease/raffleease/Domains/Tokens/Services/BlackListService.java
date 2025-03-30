package com.raffleease.raffleease.Domains.Tokens.Services;

public interface BlackListService {
    void addTokenToBlackList(String tokenId, Long expiration);
    boolean isTokenBlackListed(String tokenId);
}
