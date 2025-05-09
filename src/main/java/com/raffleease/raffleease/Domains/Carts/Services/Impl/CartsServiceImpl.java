package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.CartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Model.CartOwnerType;
import com.raffleease.raffleease.Domains.Carts.Repository.CartsRepository;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.raffleease.raffleease.Domains.Carts.Model.CartOwnerType.ADMIN;
import static com.raffleease.raffleease.Domains.Carts.Model.CartOwnerType.CUSTOMER;
import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.ACTIVE;
import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.CLOSED;

@RequiredArgsConstructor
@Service
public class CartsServiceImpl implements CartsService {
    private final TicketsService ticketsService;
    private final CartsRepository repository;
    private final CartsMapper mapper;
    
    @Override
    public CartDTO createAdminCart() {
        return create(ADMIN);
    }

    @Override
    public CartDTO createCustomerCart() {
        return create(CUSTOMER);
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

    private CartDTO create(CartOwnerType ownerType) {
        Cart newCart = save(Cart.builder()
                .status(ACTIVE)
                .ownerType(ownerType)
                .tickets(new ArrayList<>())
                .build());
        return mapper.fromCart(newCart);
    }
}
