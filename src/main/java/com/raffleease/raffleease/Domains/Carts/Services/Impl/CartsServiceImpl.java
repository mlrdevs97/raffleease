package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.CartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Repository.CartsRepository;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.ACTIVE;

@RequiredArgsConstructor
@Service
public class CartsServiceImpl implements CartsService {
    private final CartsRepository repository;
    private final CartsMapper mapper;
    
    @Override
    public CartDTO create() {
        return mapper.fromCart(save(Cart.builder()
                .status(ACTIVE)
                .tickets(new ArrayList<>())
                .build()));
    }

    @Override
    public Cart findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Cart not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while fetching cart with ID <" + id + ">: " + ex.getMessage());
        }
    }

    @Override
    public Cart save(Cart cart) {
        try {
            return repository.save(cart);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving cart: " + ex.getMessage());
        }
    }
}
