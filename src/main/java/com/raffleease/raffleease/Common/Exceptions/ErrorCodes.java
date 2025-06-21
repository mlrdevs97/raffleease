package com.raffleease.raffleease.Common.Exceptions;

public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String CONFLICT = "CONFLICT";
    public static final String FILE_STORAGE_ERROR = "FILE_STORAGE_ERROR";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String EMAIL_VERIFICATION_FAILED = "EMAIL_VERIFICATION_FAILED";
    public static final String MAIL_ERROR = "MAIL_ERROR";
    public static final String ENCRYPTION_ERROR = "ENCRYPTION_ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    public static final String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String VALUE_ALREADY_EXISTS = "VALUE_ALREADY_EXISTS";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String IMAGE_LIMIT_EXCEEDED = "IMAGE_LIMIT_EXCEEDED";
}