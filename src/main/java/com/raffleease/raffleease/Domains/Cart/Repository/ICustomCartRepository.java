package com.raffleease.raffleease.Domains.Cart.Repository;

import com.raffleease.raffleease.Domains.Cart.Model.Cart;

import java.time.LocalDateTime;
import java.util.List;

public interface ICustomCartRepository {
    void updateExpiredCart(LocalDateTime lastModified);

    List<Cart> findExpiredCarts(LocalDateTime lastModified);
}
