package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class EncryptionException extends RuntimeException {
    @Autowired
    public EncryptionException(String message) {
        super(message);
    }
}

