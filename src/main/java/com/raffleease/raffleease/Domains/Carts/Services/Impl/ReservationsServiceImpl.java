package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.CartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Services.CartLifecycleService;
import com.raffleease.raffleease.Domains.Carts.Services.CartsPersistenceService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Services.ReservationsService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesQueryService;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesStatisticsService;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsQueryService;
import com.raffleease.raffleease.Domains.Tickets.Services.TicketsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;

@RequiredArgsConstructor
@Service
public class ReservationsServiceImpl implements ReservationsService {
    private final TicketsQueryService ticketsQueryService;
    private final AssociationsService associationsService;
    private final RafflesQueryService rafflesQueryService;
    private final CartLifecycleService cartLifecycleService;
    private final CartsPersistenceService cartsPersistenceService;
    private final CartsMapper cartsMapper;
    private final TicketsService ticketsService;
    private final RafflesStatisticsService statisticsService;

    @Override
    @Transactional
    public CartDTO reserve(ReservationRequest request, Long associationId, Long cartId) {
        List<Ticket> tickets = ticketsQueryService.findAllById(request.ticketIds());
        Association association = associationsService.findById(associationId);
        validateTicketsBelongToAssociationRaffle(tickets, association);
        validateTicketsAvailability(tickets);
        Cart cart = cartsPersistenceService.findById(cartId);
        reserveTickets(cart, tickets);
        cart.getTickets().addAll(tickets);
        cartsPersistenceService.save(cart);
        return cartsMapper.fromCart(cartsPersistenceService.save(cart));
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
        validateTicketsBelongToCart(cart, tickets);
        ticketsService.releaseTickets(tickets);
        statisticsService.increaseRafflesTicketsAvailability(tickets);
        cart.getTickets().removeAll(tickets);
        cartsPersistenceService.save(cart);
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

    private void validateTicketsBelongToCart(Cart cart, List<Ticket> tickets) {
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

    private void reserveTickets(Cart cart, List<Ticket> tickets) {
        ticketsService.reserveTickets(cart, tickets);
        statisticsService.reduceRaffleTicketsAvailability(tickets);
    }
}