package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;

public class CustomerCreateBuilder {
    private String fullName = "Test User";
    private String userPhonePrefix = "+34";
    private String userPhoneNumber = "600001111";
    private String email = "test.user@example.com";

    public static CustomerBuilder builder() {
        return new CustomerBuilder();
    }

    public CustomerCreateBuilder withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public CustomerCreateBuilder withPhoneNumber(String prefix, String number) {
        this.userPhonePrefix = prefix;
        this.userPhoneNumber = number;
        return this;
    }

    public CustomerCreateBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CustomerCreate build() {
        return CustomerCreate.builder()
                .phoneNumber((userPhonePrefix != null || userPhoneNumber != null)
                            ? PhoneNumberData.builder()
                            .prefix(userPhonePrefix)
                            .nationalNumber(userPhoneNumber)
                            .build()
                        : null)
                .email(email)
                .fullName(fullName)
                .build();
    }
}