package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String msg) {
        super(msg);
    }
}
