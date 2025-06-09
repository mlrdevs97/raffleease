package com.raffleease.raffleease.Domains.Customers.Services;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import com.raffleease.raffleease.Domains.Customers.DTO.CustomerDTO;
import com.raffleease.raffleease.Domains.Customers.DTO.CustomerSearchFilters;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomersService {
    Customer create(String stripeId, String name, String email, String phoneNumber);
    Customer create(CustomerCreate customerData);
    Customer findById(Long id);
    Page<CustomerDTO> search(Long associationId, CustomerSearchFilters searchFilters, Pageable pageable);
}