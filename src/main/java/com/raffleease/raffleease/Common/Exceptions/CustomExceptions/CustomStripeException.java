package com.raffleease.raffleease.Common.Exceptions.CustomExceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class CustomStripeException extends RuntimeException {
    @Autowired
    public CustomStripeException(String msg) { super(msg); }
}
