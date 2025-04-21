package com.raffleease.raffleease.Domains.Carts.Repository;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CartsRepository extends JpaRepository<Cart, Long> {
    List<Cart> findAllByUpdatedAtBefore(LocalDateTime updatedAt);
}
