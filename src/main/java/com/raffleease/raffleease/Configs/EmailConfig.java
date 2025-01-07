package com.raffleease.raffleease.Configs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@RequiredArgsConstructor
public class EmailConfig {
    private final JavaMailSenderImpl mailSender;

    @Value("${MAIL_PASSWORD}")
    private String mailPassword;

    @PostConstruct
    public void init() {
        mailSender.setPassword(mailPassword);
    }
}