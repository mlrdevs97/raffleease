package com.raffleease.raffleease.Domains.Tickets.Services.Impls;

import com.raffleease.raffleease.Domains.Orders.DTOs.Reservation;
import com.raffleease.raffleease.Domains.Raffles.Services.IAvailabilityService;
import com.raffleease.raffleease.Domains.Tickets.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Tickets.DTO.ReservationResponse;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Mappers.TicketsMapper;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.ICustomTicketsRepository;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketReservationsService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsCommandService;
import com.raffleease.raffleease.Domains.Tickets.Services.ITicketsQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.EncryptionException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.RESERVED;

@RequiredArgsConstructor
@Service
public class TicketReservationsServiceImpl implements ITicketReservationsService {
    private final ITicketsQueryService ticketsQueryService;
    private final IAvailabilityService availabilityService;
    private final ITicketsCommandService commandService;
    private final ICustomTicketsRepository repository;
    private final TicketsMapper mapper;

    @Override
    public ReservationResponse reserve(ReservationRequest request) {
        Set<Ticket> tickets = findAllById(request.ticketsIds());
        return reserveInternal(request.raffleId(), tickets);
    }

    @Override
    public ReservationResponse reserve(Long raffleId, Set<Ticket> tickets) {
        return reserveInternal(raffleId, tickets);
    }

    @Transactional
    private ReservationResponse reserveInternal(Long raffleId, Set<Ticket> tickets) {
        checkTicketsAvailability(tickets);
        ReservationResponse reservationResponse = setReservationDetails(tickets);
        availabilityService.reduceAvailableTickets(raffleId, tickets.size());
        return reservationResponse;
    }

    @Override
    @Transactional
    public void release(ReservationRequest request) {
        Set<Ticket> tickets = findAllById(request.ticketsIds());
        removeReservationDetails(tickets);
        availabilityService.increaseAvailableTickets(request.raffleId(), tickets.size());
    }

    private Set<Ticket> findAllById(Set<Long> ticketIds) {
        Set<Ticket> tickets = new HashSet<>(ticketsQueryService.findAllById(ticketIds));
        if (tickets.isEmpty() || tickets.size() < ticketIds.size()) {
            throw new NotFoundException("One or more tickets could not be found");
        }
        return tickets;
    }

    private void checkTicketsAvailability(Set<Ticket> tickets) {
        tickets.forEach(ticket -> {
            if (ticket.getStatus() != AVAILABLE) {
                throw new BusinessException("One or more tickets are not available");
            }
        });
    }

    private void removeReservationDetails(Set<Ticket> tickets) {
        tickets.forEach(ticket -> {
            ticket.setStatus(AVAILABLE);
            ticket.setReservationFlag(null);
            ticket.setReservationTime(null);
        });
        commandService.saveAll(tickets);
    }

    private ReservationResponse setReservationDetails(Set<Ticket> tickets) {
        LocalDateTime now = LocalDateTime.now();
        String reservationFlag = generateReservationFlag(tickets, now);

        tickets.forEach(ticket -> {
            ticket.setStatus(RESERVED);
            ticket.setReservationFlag(reservationFlag);
            ticket.setReservationTime(now);
        });

        commandService.saveAll(tickets);
        Set<TicketDTO> ticketDTOS = mapper.fromTicketSet(tickets);

        return ReservationResponse.builder()
                .tickets(ticketDTOS)
                .reservationFlag(reservationFlag)
                .build();
    }

    private String generateReservationFlag(Set<Ticket> tickets, LocalDateTime now) {
        String ids = tickets.stream()
                .map(ticket -> String.valueOf(ticket.getId()))
                .sorted()
                .collect(Collectors.joining(","));
        String input = ids + now.toString();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Error generating reservation flag: " + e);
        }
    }

    @Override
    public Boolean checkReservation(Set<Reservation> reservations) {
        return reservations.stream().allMatch(reservation -> reservation.tickets().stream().allMatch(ticket ->
                ticket.status().equals(RESERVED) && ticket.reservationFlag().equals(reservation.reservationFlag())
        ));
    }

    @Override
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void releaseScheduled() {
        LocalDateTime reservationTime = LocalDateTime.now().minusMinutes(10);
        repository.updateStatusAndReservationFlag(reservationTime);
        List<Object[]> rafflesAndTicketCount = repository.findRafflesAndUpdatedTicketCount(reservationTime);

        rafflesAndTicketCount.forEach(raffleAndCount -> {
            Long raffleId = ((Number) raffleAndCount[0]).longValue();
            long updatedTicketCount = ((Number) raffleAndCount[1]).longValue();

            availabilityService.increaseAvailableTickets(raffleId, updatedTicketCount);
        });

        repository.updateReservationTime(reservationTime);
    }
}
