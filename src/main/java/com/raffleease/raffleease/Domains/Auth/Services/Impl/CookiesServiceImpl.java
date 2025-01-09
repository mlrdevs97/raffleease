package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class CookiesServiceImpl implements ICookiesService {
    @Value("${spring.application.config.is_test}")
    private boolean isTest;

    public void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
                .sameSite("None")
                .path("/api/v1/tokens/refresh")
                .maxAge(maxAge);

        if (isTest) {
            cookieBuilder.secure(false);
        } else {
            cookieBuilder.httpOnly(true).secure(true);
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, "", 0);
    }

    @Override
    public String getCookieValue(HttpServletRequest request, String cookieName) {
        return getCookie(request, cookieName).getValue();
    }

    private Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies) || cookies.length == 0) throw new AuthorizationException("No cookies found in the request");

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        throw new AuthorizationException("Cookie with name <"+ cookieName + "> not found");
    }
}
