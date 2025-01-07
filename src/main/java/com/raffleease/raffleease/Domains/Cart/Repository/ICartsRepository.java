package com.raffleease.raffleease.Domains.Cart.Repository;

import com.raffleease.raffleease.Domains.Cart.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICartsRepository extends JpaRepository<Cart, Long> {
}
