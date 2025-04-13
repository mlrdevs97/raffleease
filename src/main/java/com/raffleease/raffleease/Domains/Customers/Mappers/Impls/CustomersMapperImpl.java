package com.raffleease.raffleease.Domains.Customers.Mappers.Impls;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerDTO;
import com.raffleease.raffleease.Domains.Customers.Mappers.ICustomersMapper;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CustomersMapperImpl implements ICustomersMapper {
    @Override
    public CustomerDTO fromCustomer(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .stripeId(customer.getStripeId())
                .sourceType(customer.getSourceType())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
