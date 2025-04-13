package com.raffleease.raffleease.Domains.Customers.Services.Impl;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Model.CustomerSourceType;
import com.raffleease.raffleease.Domains.Customers.Repository.CustomersRepository;
import com.raffleease.raffleease.Domains.Customers.Services.CustomersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.raffleease.raffleease.Domains.Customers.Model.CustomerSourceType.ADMIN;
import static com.raffleease.raffleease.Domains.Customers.Model.CustomerSourceType.STRIPE;

@RequiredArgsConstructor
@Service
public class CustomersServiceImpl implements CustomersService {
    private final CustomersRepository repository;

    @Override
    public Customer create(String stripeId, String fullName, String email, String phoneNumber) {
        return save(Customer.builder()
                .stripeId(stripeId)
                .fullName(fullName)
                .email(email)
                .phoneNumber(phoneNumber)
                .sourceType(STRIPE)
                .build());
    }

    @Override
    public Customer create(CustomerCreate data) {
        String phoneNumber = Objects.nonNull(data.phoneNumber())
                ? data.phoneNumber().prefix() + data.phoneNumber().nationalNumber()
                : null;

        return save(Customer.builder()
                .fullName(data.fullName())
                .email(data.email())
                .phoneNumber(phoneNumber)
                .sourceType(ADMIN)
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

    private Customer save(Customer customer) {
        try {
            return repository.save(customer);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving new customer: " + ex.getMessage());
        }
    }
}
