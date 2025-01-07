package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class CookiesServiceImpl implements ICookiesService {
    public void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/api/v1")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, "", 0);
    }

    @Override
    public String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies) || cookies.length == 0) throw new AuthorizationException("No cookies found in the request");

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        throw new AuthorizationException("Cookie with name <"+ cookieName + "> not found");
    }
}
