package com.raffleease.raffleease.Domains.Auth.Services.Impl;

import com.raffleease.raffleease.Domains.Auth.Services.ICookiesService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Slf4j
@RequiredArgsConstructor
@Service
public class CookiesServiceImpl implements ICookiesService {
    @Value("${spring.application.config.is_test}")
    private boolean isTest;

    public void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        if (isTest) log.info("Adding cookie: {}, Max-Age: {}", name, maxAge);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge);

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(SET_COOKIE, cookie.toString());
        if (isTest) log.info("Cookie added: {}", name);
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        if (isTest) log.info("Deleting cookie: {}", name);
        addCookie(response, name, "", 0);
    }

    @Override
    public String getCookieValue(HttpServletRequest request, String cookieName) {
        if (isTest) log.info("Retrieving cookie value for: {}", cookieName);
        Cookie cookie = getCookie(request, cookieName);
        if (isTest) log.debug("Cookie found: {}", cookieName);
        return cookie.getValue();
    }

    private Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies) || cookies.length == 0) {
            throw new AuthorizationException("No cookies found in the request");
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        throw new AuthorizationException("Cookie with name <" + cookieName + "> not found");
    }
}
