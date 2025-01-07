package com.raffleease.raffleease.Domains.Token.Services;

public interface IBlackListService {
    void addTokenToBlackList(String tokenId, Long expiration);
    boolean isTokenBlackListed(String tokenId);
}
