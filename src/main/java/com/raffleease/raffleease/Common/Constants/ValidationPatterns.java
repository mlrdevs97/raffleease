package com.raffleease.raffleease.Common.Constants;

/**
 * Common validation patterns used across multiple domains.
 * Centralized location for regex patterns to ensure consistency.
 */
public final class ValidationPatterns {
    
    // Phone number patterns
    public static final String PHONE_PREFIX_PATTERN = "^\\+\\d{1,3}";
    public static final String PHONE_NATIONAL_NUMBER_PATTERN = "^\\d{1,14}$";
    
    // Password pattern - at least 8 chars, max 32, with uppercase, lowercase, digit, and special char
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%-^&_*(),.?\":{}|<>]).{8,32}$";
    
    // US ZIP code pattern - supports both 5 digit and 5+4 format, optional
    public static final String US_ZIP_CODE_PATTERN = "^$|^[0-9]{5}(?:-[0-9]{4})?$";
    
    // Email pattern (basic)
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    // Common validation messages
    public static final class Messages {
        public static final String PHONE_PREFIX_MESSAGE = "Phone prefix must start with + followed by 1-3 digits";
        public static final String PHONE_NATIONAL_NUMBER_MESSAGE = "National number must contain 1-14 digits only";
        public static final String PASSWORD_MESSAGE = "Password must be 8-32 characters with at least one uppercase, lowercase, digit and special character";
        public static final String ZIP_CODE_MESSAGE = "ZIP code must be in format 12345 or 12345-6789, or empty";
        public static final String EMAIL_MESSAGE = "Please provide a valid email address";
    }
    
    private ValidationPatterns() {}
} 