package com.raffleease.raffleease.Domains.Customers.Services;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;

public interface CustomersService {
    Customer create(String stripeId, String name, String email, String phoneNumber);
    Customer create(CustomerCreate customerData);
    Customer findById(Long id);
}
