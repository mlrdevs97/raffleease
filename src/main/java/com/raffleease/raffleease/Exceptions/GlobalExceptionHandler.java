package com.raffleease.raffleease.Exceptions;

import com.raffleease.raffleease.Exceptions.CustomExceptions.ConflictException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFoundException(NotFoundException exp) {
        ApiResponse response = ResponseFactory.error(
                exp.getMessage(),
                NOT_FOUND.value(),
                NOT_FOUND.getReasonPhrase()
        );

        return ResponseEntity
                .status(NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(ConflictException exp) {
        ApiResponse response = ResponseFactory.error(
                exp.getMessage(),
                CONFLICT.value(),
                CONFLICT.getReasonPhrase()
        );

        return ResponseEntity
                .status(CONFLICT)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exp) {
        Map<String, String> errors = new HashMap<>();

        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError)error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        ApiResponse response = ResponseFactory.validationError(
                exp.getMessage(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                errors
        );

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(response);
    }
}
