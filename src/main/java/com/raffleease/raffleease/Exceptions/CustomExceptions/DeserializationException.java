package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class DeserializationException extends RuntimeException {
    @Autowired
    public DeserializationException(String msg) { super(msg); }
}
