package com.raffleease.raffleease.Responses;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class ApiResponse {
        private final boolean status;
        private final String message;
        private final LocalDateTime timestamp;

        public ApiResponse(boolean status, String message) {
                this.status = status;
                this.message = message;
                this.timestamp = LocalDateTime.now();
        }
}