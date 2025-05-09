package com.raffleease.raffleease.Domains.Customers.DTO;

public record CustomerSearchFilters(
    String fullName,
    String email,
    String phoneNumber
) {
    public CustomerSearchFilters {
        fullName = fullName != null? fullName.trim() : null;
        email = email != null? email.trim() : null;
        phoneNumber = phoneNumber != null? phoneNumber.trim() : null;
    }
}
