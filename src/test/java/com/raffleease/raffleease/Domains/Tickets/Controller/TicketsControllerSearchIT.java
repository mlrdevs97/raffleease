package com.raffleease.raffleease.Domains.Tickets.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Customers.Repository.CustomersRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class TicketsControllerSearchIT extends BaseIT {
    @Autowired
    protected CustomersRepository customersRepository;
    private TicketsCreate ticketsCreate = new TicketsCreateBuilder().build();
    private String getTicketsPath;
    private String getRandomTicketsPath;
    private Long raffleId;

    @BeforeEach
    void setUp() throws Exception {
        raffleId = createRaffle(associationId, accessToken);
        getTicketsPath = "/api/v1/associations/" + associationId + "/raffles/" + raffleId + "/tickets";
        getRandomTicketsPath = getTicketsPath + "/random";
    }

    @Test
    void shouldReturnAllAvailableTicketsForRaffle() throws Exception {
        perGetRequest(null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(ticketsCreate.amount()))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.message").value("Ticket retrieved successfully"));
    }

    @Test
    void shouldReturnTicketByNumber() throws Exception {
        perGetRequest(Map.of("ticketNumber", "102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ticketNumber").value("102"));
    }

    @Test
    void shouldReturnTicketsByCustomer() throws Exception {
        Customer customer = createCustomer();
        assignTicketToCustomer(raffleId, "102", customer);

        perGetRequest(Map.of("customerId", customer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ticketNumber").value("102"));
    }

    @Test
    void shouldReturnSpecificTicketByNumberAndCustomer() throws Exception {
        Customer customer = createCustomer();
        assignTicketToCustomer(raffleId, "103", customer);

        perGetRequest(Map.of(
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

        mockMvc.perform(get("/api/v1/associations/" + associationId + "/raffles/" + invalidRaffleId + "/tickets")
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Raffle not found for id <" + invalidRaffleId + ">"));
    }

    @Test
    void shouldReturnNotFoundIfCustomerDoesNotExist() throws Exception {
        long nonExistentCustomerId = 999L;

        perGetRequest(Map.of("customerId", String.valueOf(nonExistentCustomerId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found for id <" + nonExistentCustomerId + ">"));
    }

    @Test
    void shouldReturnNotFoundWhenNoTicketsMatchSearch() throws Exception {
        perGetRequest(Map.of("ticketNumber", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No ticket was found for search"));
    }

    @Test
    void shouldReturnRandomTicketsSuccessfully() throws Exception {
        mockMvc.perform(get(getRandomTicketsPath)
                        .param("quantity", "3")
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.message").value("Random tickets retrieved successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenQuantityIsMissing() throws Exception {
        mockMvc.perform(get(getRandomTicketsPath)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required request parameter: 'quantity'"));
    }

    @Test
    void shouldReturnNotFoundIfRaffleDoesNotExistOnRandomRequest() throws Exception {
        long invalidRaffleId = 999L;
        String path = "/api/v1/associations/" + associationId + "/raffles/" + invalidRaffleId + "/tickets/random";

        mockMvc.perform(get(path)
                        .param("quantity", "2")
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Raffle not found for id <" + invalidRaffleId + ">"));
    }

    @Test
    void shouldReturnBusinessExceptionIfNotEnoughTicketsAvailable() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        List<Ticket> tickets = ticketsRepository.findAllByRaffle(raffle).stream()
                .peek(ticket -> ticket.setStatus(SOLD))
                .toList();
        ticketsRepository.saveAll(tickets);

        mockMvc.perform(get(getRandomTicketsPath)
                        .param("quantity", "2")
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not enough tickets were found for this order"));
    }

    @Test
    void shouldReturnBusinessExceptionIfQuantityExceedsAvailableTickets() throws Exception {
        mockMvc.perform(get(getRandomTicketsPath)
                        .param("quantity", String.valueOf(ticketsCreate.amount() + 1))
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not enough tickets were found for this order"));
    }

    private ResultActions perGetRequest(Map<String, String> params) throws Exception {
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