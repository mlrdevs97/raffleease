package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerBuilder {
    private String stripeId = "cus_" + UUID.randomUUID();
    private String fullName = "Test User";
    private String phoneNumber = "+34600000000";
    private String email = "test.user@example.com";
    private LocalDateTime createdAt = LocalDateTime.now();

    public static CustomerBuilder builder() {
        return new CustomerBuilder();
    }

    public CustomerBuilder withStripeId(String stripeId) {
        this.stripeId = stripeId;
        return this;
    }

    public CustomerBuilder withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public CustomerBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public CustomerBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CustomerBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Customer build() {
        Customer customer = new Customer();
        customer.setStripeId(stripeId);
        customer.setFullName(fullName);
        customer.setPhoneNumber(phoneNumber);
        customer.setEmail(email);
        customer.setCreatedAt(createdAt);
        return customer;
    }
}