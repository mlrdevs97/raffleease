package com.raffleease.raffleease.Domains.Customers.Services;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;

public interface CustomersService {
    Customer createCustomer(String stripeId, String name, String email, String phoneNumber);
    Customer findById(Long id);
}
