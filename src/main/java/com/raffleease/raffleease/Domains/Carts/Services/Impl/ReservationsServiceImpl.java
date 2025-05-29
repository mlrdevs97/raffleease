package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.CartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RaffleTicketsAvailabilityService;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Services.ReservationsService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesQueryService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsQueryService;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.RESERVED;

@RequiredArgsConstructor
@Service
public class ReservationsServiceImpl implements ReservationsService {
    private final TicketsQueryService ticketsQueryService;
    private final RaffleTicketsAvailabilityService raffleTicketsAvailabilityService;
    private final TicketsService ticketsService;
    private final AssociationsService associationsService;
    private final RafflesQueryService rafflesQueryService;
    private final CartsService cartsService;
    private final CartsMapper cartsMapper;

    @Override
    @Transactional
    public CartDTO reserve(ReservationRequest request, Long associationId, Long cartId) {
        List<Ticket> tickets = ticketsQueryService.findAllById(request.ticketsIds());
        Association association = associationsService.findById(associationId);
        validateTicketsBelongToAssociationRaffle(tickets, association);
        validateTicketsAvailability(tickets);
        Cart cart = cartsService.findById(cartId);
        reserveTickets(tickets, cart);
        reduceRaffleTicketsAvailability(tickets);
        cart.getTickets().addAll(tickets);
        cartsService.save(cart);
        return cartsMapper.fromCart(cartsService.save(cart));
    }

    @Override
    @Transactional
    public void release(List<Ticket> tickets) {
        releaseTickets(tickets);
    }

    @Override
    @Transactional
    public void release(Cart cart) {
        releaseTicketsFromCart(cart, cart.getTickets());
    }

    @Override
    @Transactional
    public void release(ReservationRequest request, Long associationId, Long cartId) {
        Cart cart = cartsService.findById(cartId);
        List<Ticket> tickets = ticketsQueryService.findAllById(request.ticketsIds());
        Association association = associationsService.findById(associationId);
        validateTicketsBelongToAssociationRaffle(tickets, association);
        releaseTicketsFromCart(cart, tickets);
    }

    private void releaseTicketsFromCart(Cart cart, List<Ticket> tickets) {
        checkTicketsBelongToCart(cart, tickets);
        releaseTickets(tickets);
        cart.getTickets().removeAll(tickets);
        cartsService.save(cart);
    }

    private void releaseTickets(List<Ticket> tickets) {
        ticketsService.saveAll(tickets.stream().peek(ticket -> {
            ticket.setStatus(AVAILABLE);
            ticket.setCart(null);
        }).toList());
        increaseRafflesTicketsAvailability(tickets);
    }

    private void validateTicketsBelongToAssociationRaffle(List<Ticket> tickets, Association association) {
        Set<Raffle> associationRaffles = new HashSet<>(rafflesQueryService.findAllByAssociation(association));
        Set<Raffle> ticketsRaffles = tickets.stream().map(Ticket::getRaffle).collect(Collectors.toSet());

        boolean anyNotBelong = ticketsRaffles.stream().anyMatch(raffle -> !associationRaffles.contains(raffle));
        if (anyNotBelong) {
            throw new BusinessException("Some tickets do not belong to an association raffle");
        }
    }

    private void validateTicketsAvailability(List<Ticket> tickets) {
        boolean anyUnavailable = tickets.stream().anyMatch(ticket -> ticket.getStatus() != AVAILABLE);
        if (anyUnavailable) {
            throw new BusinessException("Some tickets are not available");
        }
    }

    private void checkTicketsBelongToCart(Cart cart, List<Ticket> tickets) {
        Set<Ticket> cartTickets = new HashSet<>(cart.getTickets());
        if (tickets.stream().anyMatch(ticket -> !cartTickets.contains(ticket))) {
            throw new BusinessException("Cannot release a ticket that does not belong to the cart");
        }
    }

    private void reserveTickets(List<Ticket> tickets, Cart cart) {
        tickets.forEach(ticket -> {
            ticket.setStatus(RESERVED);
            ticket.setCart(cart);
        });
    }

    private void reduceRaffleTicketsAvailability(List<Ticket> tickets) {
        Map<Raffle, Long> ticketsByRaffle = tickets.stream().collect(
                Collectors.groupingBy(Ticket::getRaffle, Collectors.counting())
        );
        ticketsByRaffle.forEach(raffleTicketsAvailabilityService::reduceAvailableTickets);
    }

    private void increaseRafflesTicketsAvailability(List<Ticket> tickets) {
        Map<Raffle, Long> ticketsByRaffle = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getRaffle, Collectors.counting()));
        ticketsByRaffle.forEach(raffleTicketsAvailabilityService::increaseAvailableTickets);
    }
}