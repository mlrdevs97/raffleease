package com.raffleease.raffleease.Domains.Customers.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CustomerDTO(

        @NotBlank(message = "Must provide an id")
        String stripeId,

        @NotBlank(message = "Firstname should not be blank")
        String name,

        @NotBlank(message = "Email should not be blank")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Phone number should not be blank")
        @Pattern(regexp = "^([67])[0-9]{8}$", message = "Invalid phone number format")
        String phoneNumber
) {
}