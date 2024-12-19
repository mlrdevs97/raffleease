package com.raffleease.raffleease.Responses;

import java.util.Map;

public class ResponseFactory {
    public static <T> ApiResponse success(String message) {
        return new SuccessResponse<>(null, message);
    }

    public static <T> ApiResponse success(T data, String message) {
        return new SuccessResponse<>(data, message);
    }

    public static ApiResponse error(String message, int errorCode, String reason) {
        return new ErrorResponse(message, errorCode, reason);
    }

    public static ApiResponse validationError(String message, int errorCode, String reason, Map<String, String> errors) {
        return new ValidationErrorResponse(message, errorCode, reason, errors);
    }
}
