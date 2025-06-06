package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;

import java.util.List;

public interface TicketsService {
    List<Ticket> create(Raffle raffle, TicketsCreate request);
    void releaseTickets(List<Ticket> tickets);
    void reserveTickets(Cart cart, List<Ticket> tickets);
    List<Ticket> transferTicketsToCustomer(List<Ticket> cartTickets, Customer customer);
    List<Ticket> updateStatus(List<Ticket> tickets, TicketStatus status);
}