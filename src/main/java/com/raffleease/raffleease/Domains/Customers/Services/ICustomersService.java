package com.raffleease.raffleease.Domains.Customers.Services;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;

public interface ICustomersService {
    Customer createCustomer(String stripeId, String name, String email, String phoneNumber);
}
