package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.CartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Services.CartsPersistenceService;
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
    private final CartsPersistenceService cartsPersistenceService;
    private final CartsMapper cartsMapper;

    @Override
    @Transactional
    public CartDTO reserve(ReservationRequest request, Long associationId, Long cartId) {
        List<Ticket> tickets = ticketsQueryService.findAllById(request.ticketIds());
        Association association = associationsService.findById(associationId);
        validateTicketsBelongToAssociationRaffle(tickets, association);
        validateTicketsAvailability(tickets);
        Cart cart = cartsPersistenceService.findById(cartId);
        reserveTickets(tickets, cart);
        reduceRaffleTicketsAvailability(tickets);
        cart.getTickets().addAll(tickets);
        cartsPersistenceService.save(cart);
        return cartsMapper.fromCart(cartsPersistenceService.save(cart));
    }

    /**
     * Releases tickets without cart manipulation.
     * Use this when tickets are associated to an order but not a cart.
     */
    @Override
    @Transactional
    public void release(List<Ticket> tickets) {
        releaseTickets(tickets);
    }

    /**
     * Releases ALL tickets from a cart and clears the cart.
     * Use this for complete cart abandonment/expiration.
     */
    @Override
    @Transactional
    public void release(Cart cart) {
        List<Ticket> cartTickets = List.copyOf(cart.getTickets());
        releaseTicketsFromCart(cart, cartTickets);
    }

    /**
     * Releases specific tickets from a cart (partial release).
     * Validates that tickets belong to the association and cart.
     */
    @Override
    @Transactional
    public void release(ReservationRequest request, Long associationId, Long cartId) {
        Cart cart = cartsPersistenceService.findById(cartId);
        List<Ticket> tickets = ticketsQueryService.findAllById(request.ticketIds());
        Association association = associationsService.findById(associationId);
        validateTicketsBelongToAssociationRaffle(tickets, association);
        releaseTicketsFromCart(cart, tickets);
    }

    private void releaseTicketsFromCart(Cart cart, List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return; 
        }
        
        checkTicketsBelongToCart(cart, tickets);
        releaseTickets(tickets);
        
        if (cart.getTickets() != null) {
            cart.getTickets().removeAll(tickets);
        }
        cartsPersistenceService.save(cart);
    }

    private void releaseTickets(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return;
        }
        
        ticketsService.saveAll(tickets.stream().peek(ticket -> {
            ticket.setStatus(AVAILABLE);
            ticket.setCustomer(null);
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
        if (cart.getTickets() == null || cart.getTickets().isEmpty()) {
            throw new BusinessException("Cart has no tickets to release");
        }
        
        Set<Long> cartTicketIds = cart.getTickets().stream()
                .map(Ticket::getId)
                .collect(Collectors.toSet());
        
        List<Long> invalidTicketIds = tickets.stream()
                .map(Ticket::getId)
                .filter(id -> !cartTicketIds.contains(id))
                .toList();
                
        if (!invalidTicketIds.isEmpty()) {
            throw new BusinessException("Cannot release tickets that do not belong to the cart. Invalid ticket IDs: " + invalidTicketIds);
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