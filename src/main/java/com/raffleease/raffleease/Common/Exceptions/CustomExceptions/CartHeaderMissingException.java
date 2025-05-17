package com.raffleease.raffleease.Common.Exceptions.CustomExceptions;

import lombok.Getter;

@Getter
public class CartHeaderMissingException extends RuntimeException {
    public CartHeaderMissingException(String msg) { super(msg); }
}
