package com.raffleease.raffleease.Responses;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationErrorResponse extends ErrorResponse {
    private final Map<String, String> errors;

    public ValidationErrorResponse(String message, int errorCode, String reason, Map<String, String> errors) {
        super(message, errorCode, reason);
        this.errors = errors;
    }
}
