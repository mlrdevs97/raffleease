package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;
import com.raffleease.raffleease.Domains.Carts.Repository.ICartsRepository;
import com.raffleease.raffleease.Domains.Carts.Services.ICartsService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensCreateService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.ACTIVE;

@RequiredArgsConstructor
@Service
public class CartsServiceImpl implements ICartsService {
    private final ICartsRepository repository;
    private final TokensCreateService tokenCreateService;

    @Override
    public Cart create(Raffle raffle, List<Ticket> tickets) {
        Cart cart = save(
                Cart.builder()
                        .raffle(raffle)
                        .tickets(tickets)
                        .status(ACTIVE)
                        .lastModified(LocalDateTime.now())
                        .build()
        );
        return save(cart);
    }

    @Override
    public Cart addTickets(Cart cart, List<Ticket> tickets) {
        cart.getTickets().addAll(tickets);
        return save(cart);
    }

    @Override
    public void removeTickets(Cart cart, List<Ticket> tickets) {
        Set<Ticket> cartTickets = new HashSet<>(cart.getTickets());
        if (tickets.stream().anyMatch(ticket -> !cartTickets.contains(ticket))) {
            throw new BusinessException("Cannot release a ticket that does not belong to the cart");
        }
        cart.getTickets().removeAll(tickets);
        save(cart);
    }

    @Override
    public Cart edit(Cart cart, CartStatus status) {
        cart.setStatus(status);
        return save(cart);
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
    public Cart save(Cart entity) {
        try {
            return repository.save(entity);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving cart: " + ex.getMessage());
        }
    }
}
