package com.raffleease.raffleease.Domains.Carts.Services;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;

public interface CartsService {
    CartDTO createAdminCart();
    CartDTO createCustomerCart();
    Cart closeCart(Cart cart);
    Cart save(Cart entity);
    Cart findById(Long id);
}
