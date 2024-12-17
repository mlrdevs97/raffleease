package com.raffleease.raffleease.Responses;

import lombok.Getter;

@Getter
public class ErrorResponse extends ApiResponse {
    private final int errorCode;
    private final String reason;

    public ErrorResponse(String message, int errorCode, String reason) {
        super(false, message);
        this.errorCode = errorCode;
        this.reason = reason;
    }
}
