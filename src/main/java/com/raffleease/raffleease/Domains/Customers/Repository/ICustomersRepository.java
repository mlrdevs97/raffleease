package com.raffleease.raffleease.Domains.Customers.Repository;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomersRepository extends JpaRepository<Customer, Long> {
}
