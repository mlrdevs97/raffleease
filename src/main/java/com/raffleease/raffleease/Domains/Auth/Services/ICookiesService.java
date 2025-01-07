package com.raffleease.raffleease.Domains.Auth.Services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ICookiesService {
    void addCookie(HttpServletResponse response, String name, String value, long maxAge);

    void deleteCookie(HttpServletResponse response, String name);

    String extractCookieValue(HttpServletRequest request, String cookieName);
}
