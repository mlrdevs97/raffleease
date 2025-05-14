package com.raffleease.raffleease.Exceptions;

import com.raffleease.raffleease.Exceptions.CustomExceptions.*;
import com.raffleease.raffleease.Helpers.ConstraintNameMapper;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ConstraintNameMapper constraintNameMapper;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFoundException(NotFoundException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                NOT_FOUND.value(),
                NOT_FOUND.getReasonPhrase()
        );

        return ResponseEntity
                .status(NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(CustomMailException.class)
    public ResponseEntity<ApiResponse> handleCustomMailException(CustomMailException ex) {
        ApiResponse apiError = ResponseFactory.error(
                ex.getMessage(),
                INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR.getReasonPhrase()
        );
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse apiError = ResponseFactory.error(
                ex.getMessage(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase()
        );
        return ResponseEntity.status(BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<ApiResponse> handleEmailVerificationException(EmailVerificationException ex) {
        ApiResponse apiError = ResponseFactory.error(
                ex.getMessage(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase()
        );
        return ResponseEntity.status(BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse> handleFileStorageException(FileStorageException ex) {
        ApiResponse apiError = ResponseFactory.error(
                ex.getMessage(),
                INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR.getReasonPhrase()
        );
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNameNotFoundException(UsernameNotFoundException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                UNAUTHORIZED.value(),
                UNAUTHORIZED.getReasonPhrase()
        );

        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                UNAUTHORIZED.value(),
                UNAUTHORIZED.getReasonPhrase()
        );

        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse> handleAuthorizationException(AuthorizationException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                FORBIDDEN.value(),
                FORBIDDEN.getReasonPhrase()
        );

        return ResponseEntity
                .status(FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                FORBIDDEN.value(),
                FORBIDDEN.getReasonPhrase()
        );

        return ResponseEntity
                .status(FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(ConflictException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                CONFLICT.value(),
                CONFLICT.getReasonPhrase()
        );

        return ResponseEntity
                .status(CONFLICT)
                .body(response);
    }

    @ExceptionHandler(CartHeaderMissingException.class)
    public ResponseEntity<ApiResponse> handleCartHeaderMissingException(CartHeaderMissingException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase()
        );

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase()
        );

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiResponse error = ResponseFactory.error(
                "Invalid request payload.",
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase()
        );

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(EncryptionException.class)
    public ResponseEntity<ApiResponse> handleEncryptionException(EncryptionException ex) {
        ApiResponse response = ResponseFactory.error(
                ex.getMessage(),
                INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR.getReasonPhrase()
        );

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        ApiResponse response = ResponseFactory.error(
                "An unexpected error occurred: " + ex.toString(),
                INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR.getReasonPhrase()

        );

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String message = String.format("Missing required request parameter: '%s'", ex.getParameterName());

        ApiResponse response = ResponseFactory.error(
                message,
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase()
        );

        return ResponseEntity.status(BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UniqueConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleUniqueConstraintViolationException(UniqueConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        String field = constraintNameMapper.mapToField(ex.getField());
        errors.put(field, "This value is already in use");

        ApiResponse response = ResponseFactory.validationError(
                "Validation failed",
                CONFLICT.value(),
                CONFLICT.getReasonPhrase(),
                errors
        );

        return ResponseEntity.status(CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                String fieldName = fieldError.getField();
                String errorMessage;

                if (fieldError.getCode() != null && fieldError.getCode().equals("typeMismatch")) {
                    Object rejectedValue = fieldError.getRejectedValue();
                    String rejected = rejectedValue != null ? rejectedValue.toString() : "null";
                    errorMessage = String.format("Invalid value '%s' for parameter '%s'", rejected, fieldName);
                } else {
                    errorMessage = fieldError.getDefaultMessage();
                }

                errors.put(fieldName, errorMessage);
            }
        });

        ApiResponse response = ResponseFactory.validationError(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
