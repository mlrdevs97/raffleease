package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.CompletionReason.MANUALLY_COMPLETED;
import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.*;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RafflesControllerUpdateStatusIT extends BaseRafflesIT {
    private Long raffleId;

    @BeforeEach
    void setUp() throws Exception {
        raffleId = createRaffle();
    }

    @Test
    void shouldUpdateStatusFromPendingToActive() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(ACTIVE.toString()))
                .andExpect(jsonPath("$.message").value("Raffle status updated successfully"));

        Raffle updated = rafflesRepository.findById(raffleId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ACTIVE);
        assertThat(updated.getStartDate()).isNotNull();
    }

    @Test
    void shouldUpdateStatusFromPausedToActive() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());

        performUpdateStatusRequest(raffleId, PAUSED).andExpect(status().isOk());

        performUpdateStatusRequest(raffleId, ACTIVE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(ACTIVE.toString()));

        Raffle updated = rafflesRepository.findById(raffleId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ACTIVE);
        assertThat(updated.getStartDate()).isNotNull();
    }

    @Test
    void shouldFailUpdateStatusFromOtherStatusToActive() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());

        performUpdateStatusRequest(raffleId, ACTIVE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status transition to ACTIVE"));
    }

    @Test
    void shouldPauseRaffleIfStatusIsActive() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());

        performUpdateStatusRequest(raffleId, PAUSED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(PAUSED.toString()));

        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        assertThat(raffle.getStatus()).isEqualTo(PAUSED);
    }

    @Test
    void shouldFailPauseIfStatusIsNotActive() throws Exception {
        performUpdateStatusRequest(raffleId, PAUSED)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only raffles in 'ACTIVE' state can be paused."));

        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        assertThat(raffle.getStatus()).isEqualTo(PENDING);
    }

    @Test
    void shouldCompleteRaffleIfStatusIsActive() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());

        performUpdateStatusRequest(raffleId, COMPLETED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(COMPLETED.toString()));

        Raffle updated = rafflesRepository.findById(raffleId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
        assertThat(updated.getCompletionReason()).isEqualTo(MANUALLY_COMPLETED);
    }

    @Test
    void shouldReactivateCompletedRaffleIfValid() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());
        performUpdateStatusRequest(raffleId, COMPLETED).andExpect(status().isOk());

        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        raffle.setEndDate(LocalDateTime.now().plusDays(2));
        raffle.setAvailableTickets(1L);
        raffle.setTotalTickets(10L);
        raffle.setSoldTickets(9L);
        rafflesRepository.save(raffle);

        performUpdateStatusRequest(raffleId, ACTIVE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(ACTIVE.toString()));

        Raffle updated = rafflesRepository.findById(raffleId).orElseThrow();
        assertThat(updated.getCompletedAt()).isNull();
        assertThat(updated.getCompletionReason()).isNull();
    }

    @Test
    @Transactional
    void shouldFailToReactivateIfWinnerWasSet() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());
        performUpdateStatusRequest(raffleId, COMPLETED).andExpect(status().isOk());

        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("A-001");
        ticket.setStatus(SOLD);
        ticket.setRaffle(raffle);
        ticket = ticketsRepository.save(ticket);

        raffle.setWinningTicket(ticket);
        raffle.getTickets().add(ticket);
        rafflesRepository.save(raffle);

        performUpdateStatusRequest(raffleId, ACTIVE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot reactivate a raffle that already has a winner"));
    }

    @Test
    void shouldFailToReactivateIfEndDateIsTooClose() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());
        performUpdateStatusRequest(raffleId, COMPLETED).andExpect(status().isOk());

        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        raffle.setEndDate(LocalDateTime.now().plusHours(2));
        rafflesRepository.save(raffle);

        performUpdateStatusRequest(raffleId, ACTIVE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The end date of the raffle must be at least one day after the current date to reactivate"));
    }

    @Test
    void shouldFailToReactivateIfNoAvailableTickets() throws Exception {
        performUpdateStatusRequest(raffleId, ACTIVE).andExpect(status().isOk());
        performUpdateStatusRequest(raffleId, COMPLETED).andExpect(status().isOk());

        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        raffle.setEndDate(LocalDateTime.now().plusDays(2));
        raffle.setAvailableTickets(0L);
        raffle.setSoldTickets(10L);
        raffle.setTotalTickets(10L);
        rafflesRepository.save(raffle);

        performUpdateStatusRequest(raffleId, ACTIVE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Available tickets for raffle are required to reactivate"));
    }

    @Test
    void shouldFailIfNewStatusIsPending() throws Exception {
        performUpdateStatusRequest(raffleId, PENDING)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot revert to 'PENDING' state."));

        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        assertThat(raffle.getStatus()).isEqualTo(PENDING);
    }
}