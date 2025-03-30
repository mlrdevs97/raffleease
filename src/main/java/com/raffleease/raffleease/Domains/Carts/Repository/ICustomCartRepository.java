package com.raffleease.raffleease.Domains.Carts.Repository;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;

import java.time.LocalDateTime;
import java.util.List;

public interface ICustomCartRepository {
    void updateExpiredCart(LocalDateTime lastModified);

    List<Cart> findExpiredCarts(LocalDateTime lastModified);
}
