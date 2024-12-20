package com.raffleease.raffleease.Domains.Payments.Repository;

import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentsRepository extends JpaRepository<Payment, Long> {
}
