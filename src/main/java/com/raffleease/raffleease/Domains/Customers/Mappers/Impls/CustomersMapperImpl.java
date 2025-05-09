package com.raffleease.raffleease.Domains.Customers.Mappers.Impls;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerDTO;
import com.raffleease.raffleease.Domains.Customers.Mappers.CustomersMapper;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomersMapperImpl implements CustomersMapper {
    @Override
    public CustomerDTO fromCustomer(Customer customer) {
        return CustomerDTO.builder()
                .stripeId(customer.getStripeId())
                .sourceType(customer.getSourceType())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    @Override
    public List<CustomerDTO> fromCustomerList(List<Customer> searchResults) {
        return searchResults.stream().map(this::fromCustomer).toList();
    }
}
