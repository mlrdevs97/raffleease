package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;

@Getter
public class AuthorizationException extends RuntimeException{
    public AuthorizationException(String msg) { super(msg); }
}
