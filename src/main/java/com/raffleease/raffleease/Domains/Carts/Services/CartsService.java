package com.raffleease.raffleease.Domains.Carts.Services;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;

public interface CartsService {
    CartDTO create();
    Cart save(Cart entity);
    CartDTO get(Long cartId);
    CartDTO getUserCart();
}
