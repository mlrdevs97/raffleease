package com.raffleease.raffleease.Domains.Tickets.Controller;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Repository.CustomersRepository;
import com.raffleease.raffleease.Domains.Raffles.Controller.BaseRafflesIT;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Helpers.CustomerBuilder;
import com.raffleease.raffleease.Helpers.TicketsCreateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.Objects;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class TicketsControllerSearchIT extends BaseRafflesIT {

    @Autowired
    protected CustomersRepository customersRepository;

    private TicketsCreate ticketsCreate = new TicketsCreateBuilder().build();
    private String getTicketsPath;

    @BeforeEach
    void setUp() throws Exception {
        createRaffle();
        getTicketsPath = "/api/v1/raffles/" + raffleId + "/tickets";
    }

    @Test
    void shouldReturnAllAvailableTicketsForRaffle() throws Exception {
        performSearch(null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(ticketsCreate.amount()))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.message").value("Ticket retrieved successfully"));
    }

    @Test
    void shouldReturnTicketByNumber() throws Exception {
        performSearch(Map.of("ticketNumber", "102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ticketNumber").value("102"));
    }

    @Test
    void shouldReturnTicketsByCustomer() throws Exception {
        Customer customer = createCustomer();
        assignTicketToCustomer(raffleId, "102", customer);

        performSearch(Map.of("customerId", customer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ticketNumber").value("102"));
    }

    @Test
    void shouldReturnSpecificTicketByNumberAndCustomer() throws Exception {
        Customer customer = createCustomer();
        assignTicketToCustomer(raffleId, "103", customer);

        performSearch(Map.of(
                "ticketNumber", "103",
                "customerId", customer.getId().toString()
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ticketNumber").value("103"));
    }

    @Test
    void shouldReturnNotFoundIfRaffleDoesNotExist() throws Exception {
        long invalidRaffleId = 999L;

        mockMvc.perform(get("/api/v1/raffles/" + invalidRaffleId + "/tickets")
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Raffle not found for id <" + invalidRaffleId + ">"));
    }

    @Test
    void shouldReturnNotFoundIfCustomerDoesNotExist() throws Exception {
        long nonExistentCustomerId = 999L;

        performSearch(Map.of("customerId", String.valueOf(nonExistentCustomerId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found for id <" + nonExistentCustomerId + ">"));
    }

    @Test
    void shouldReturnNotFoundWhenNoTicketsMatchSearch() throws Exception {
        performSearch(Map.of("ticketNumber", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No ticket was found for search"));
    }

    private ResultActions performSearch(Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder request = get(getTicketsPath)
                .header(AUTHORIZATION, "Bearer " + accessToken);

        if (Objects.nonNull(params)) {
            params.forEach(request::param);
        }

        return mockMvc.perform(request);
    }

    private Customer createCustomer() {
        Customer customer = new CustomerBuilder().build();
        return customersRepository.save(customer);
    }

    private void assignTicketToCustomer(Long raffleId, String ticketNumber, Customer customer) {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        Ticket ticket = ticketsRepository.findByRaffleAndTicketNumber(raffle, ticketNumber).orElseThrow();

        ticket.setCustomer(customer);
        ticket.setStatus(SOLD);
        ticketsRepository.save(ticket);
    }
}