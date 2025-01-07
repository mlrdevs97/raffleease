package com.raffleease.raffleease.Domains.Customers.Mappers;


import com.raffleease.raffleease.Domains.Customers.DTO.CustomerDTO;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;

public interface ICustomersMapper {
    CustomerDTO fromCustomer(Customer customer);
}

