package com.raffleease.raffleease.Domains.Carts.Services;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Model.CartStatus;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;

import java.util.List;

public interface ICartsService {
    Cart create(Raffle raffle, List<Ticket> tickets);
    Cart addTickets(Cart cart, List<Ticket> tickets);
    void removeTickets(Cart cart, List<Ticket> tickets);
    Cart edit(Cart cart, CartStatus status);
    Cart save(Cart entity);
    Cart findById(Long id);
}
