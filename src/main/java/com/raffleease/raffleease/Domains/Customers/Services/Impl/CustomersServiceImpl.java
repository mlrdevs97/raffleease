package com.raffleease.raffleease.Domains.Customers.Services.Impl;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Repository.CustomersRepository;
import com.raffleease.raffleease.Domains.Customers.Services.ICustomersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CustomersServiceImpl implements ICustomersService {
    private final CustomersRepository repository;

    @Override
    public Customer createCustomer(String stripeId, String fullName, String email, String phoneNumber) {
        Customer customer = Customer.builder()
                .stripeId(stripeId)
                .fullName(fullName)
                .email(email)
                .phoneNumber(phoneNumber)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(customer);
    }
}
