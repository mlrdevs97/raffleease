package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;

@Getter
public class CartHeaderMissingException extends RuntimeException {
    public CartHeaderMissingException(String msg) { super(msg); }
}
