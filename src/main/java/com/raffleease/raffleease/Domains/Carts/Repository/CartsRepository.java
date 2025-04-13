package com.raffleease.raffleease.Domains.Carts.Repository;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartsRepository extends JpaRepository<Cart, Long> {
}
