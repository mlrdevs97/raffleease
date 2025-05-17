package com.raffleease.raffleease.Common.Exceptions.CustomExceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class BusinessException extends RuntimeException {
    @Autowired
    public BusinessException(String message) {
        super(message);
    }
}