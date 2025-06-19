package com.raffleease.raffleease.Common.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import static com.raffleease.raffleease.Common.Utils.SanitizeUtils.trimAndLower;

@Builder
@Getter
public class BaseUserData {
    @NotBlank
    @Size(min = 2, max = 50)
    private final String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private final String lastName;

    @NotBlank
    @Size(min = 2, max = 25)
    private final String userName;

    @NotBlank
    @Email
    private final String email;

    @Nullable
    @Valid
    private final PhoneNumber phoneNumber;

    @JsonCreator
    public BaseUserData(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("userName") String userName,
            @JsonProperty("email") String email,
            @JsonProperty("phoneNumber") PhoneNumber phoneNumber
    ) {
        this.firstName = trimAndLower(firstName);
        this.lastName = trimAndLower(lastName);
        this.userName = trimAndLower(userName);
        this.email = trimAndLower(email);
        this.phoneNumber = phoneNumber;
    }
}
