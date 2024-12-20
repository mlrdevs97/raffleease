package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class InvalidSignatureException extends RuntimeException{
    @Autowired
    public InvalidSignatureException(String msg) { super(msg); }
}
