package com.raffleease.raffleease.Domains.Cart.Mappers;

import com.raffleease.raffleease.Domains.Cart.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Cart.Model.Cart;
import org.springframework.stereotype.Service;

@Service
public interface ICartsMapper {
    CartDTO fromCart(Cart cart);
}
