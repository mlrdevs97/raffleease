package com.raffleease.raffleease.Responses;

import lombok.Getter;

@Getter
public class ErrorResponse extends ApiResponse {
    private final int status;
    private final String statusText;

    public ErrorResponse(String message, int status, String statusText) {
        super(false, message);
        this.status = status;
        this.statusText = statusText;
    }
}
