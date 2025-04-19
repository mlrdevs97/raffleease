package com.raffleease.raffleease.Domains.Customers.DTO;

import com.raffleease.raffleease.Domains.Customers.Model.CustomerSourceType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDateTime;

// TODO: Check use cases and validations
@Builder
public record CustomerDTO(
        @NotBlank(message = "Must provide an id")
        String stripeId,

        CustomerSourceType sourceType,

        @NotBlank(message = "Must provide a name for user")
        String fullName,

        @NotBlank(message = "Email should not be blank")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Phone number should not be blank")
        @Pattern(regexp = "^([67])[0-9]{8}$", message = "Invalid phone number format")
        String phoneNumber,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}