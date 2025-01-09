package com.raffleease.raffleease.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.http.HttpHeaders.*;

@Configuration
public class CorsConfig {
    @Value("${spring.application.config.cors.allowed_origin}")
    private String allowedOrigin;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(
                Collections.singletonList(allowedOrigin)
        );
        config.setAllowedHeaders(
                Arrays.asList(
                        ORIGIN,
                        CONTENT_TYPE,
                        ACCEPT,
                        AUTHORIZATION
                )
        );
        config.setAllowedMethods(
                Arrays.asList(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}