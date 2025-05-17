package com.raffleease.raffleease.Common.Exceptions.CustomExceptions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class StripeWebHookException extends RuntimeException {
    @Autowired
    public StripeWebHookException(String msg) { super(msg); }
}
