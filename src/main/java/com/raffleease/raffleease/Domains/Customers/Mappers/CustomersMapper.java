package com.raffleease.raffleease.Domains.Customers.Mappers;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerDTO;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;

import java.util.List;

public interface CustomersMapper {
    CustomerDTO fromCustomer(Customer customer);
    List<CustomerDTO> fromCustomerList(List<Customer> searchResults);
}

