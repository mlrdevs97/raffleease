package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class EmailVerificationException extends RuntimeException {
    @Autowired
    public EmailVerificationException(String message) {
        super(message);
    }
}
