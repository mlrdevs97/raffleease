package com.raffleease.raffleease.Domains.Customers.Services.Impl;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Repository.CustomersRepository;
import com.raffleease.raffleease.Domains.Customers.Services.CustomersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CustomersServiceImpl implements CustomersService {
    private final CustomersRepository repository;

    @Override
    public Customer createCustomer(String stripeId, String fullName, String email, String phoneNumber) {
        return repository.save(Customer.builder()
                .stripeId(stripeId)
                .fullName(fullName)
                .email(email)
                .phoneNumber(phoneNumber)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Customer findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while fetching customer with ID <" + id + ">: " + ex.getMessage());
        }
    }
}
