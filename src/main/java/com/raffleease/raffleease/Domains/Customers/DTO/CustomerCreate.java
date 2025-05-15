package com.raffleease.raffleease.Domains.Customers.DTO;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CustomerCreate(
        @NotBlank
        String fullName,

        @Nullable
        @Email
        String email,

        @Nullable
        @Valid
        PhoneNumberData phoneNumber
) {
        public CustomerCreate {
                fullName = trim(fullName);
                email = email == null ? null : trim(email).toLowerCase();
                phoneNumber = phoneNumber == null ? null : new PhoneNumberData(
                        trim(phoneNumber.prefix()),
                        trim(phoneNumber.nationalNumber())
                );
        }

        private static String trim(String value) {
                return value == null ? null : value.trim();
        }
}

