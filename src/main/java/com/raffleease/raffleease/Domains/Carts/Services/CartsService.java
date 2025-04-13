package com.raffleease.raffleease.Domains.Carts.Services;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;

public interface CartsService {
    CartDTO createAdmin();
    CartDTO createCustomer();
    Cart edit(Cart cart, CartStatus status);
    Cart save(Cart entity);
    Cart findById(Long id);
}
