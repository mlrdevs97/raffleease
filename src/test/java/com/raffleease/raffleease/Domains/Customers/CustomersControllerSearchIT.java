package com.raffleease.raffleease.Domains.Customers;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.PhoneNumberData;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Orders.DTOs.AdminOrderCreate;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Helpers.AdminOrderCreateBuilder;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class CustomersControllerSearchIT extends BaseIT {
    private String customersPath;
    Raffle raffle;

    @BeforeEach
    void setUp() throws Exception {
        customersPath = "/api/v1/associations/" + associationId + "/customers";
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        Long raffleId = createRaffle(images, associationId, accessToken);
        raffle = rafflesRepository.findById(raffleId).orElseThrow();
    }

    @Test
    void shouldReturnAllCustomersWithPagination() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("First Customer").email("first@customer.com").phone("111111111").build(), 0);
        performOrderForNewReservation(CustomerData.builder().name("Second Customer").email("second@customer.com").phone("111111112").build(), 1);
        performOrderForNewReservation(CustomerData.builder().name("Third Customer").email("third@customer.com").phone("111111113").build(), 2);

        perGetRequest(Map.of("page", "0", "size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.page.size").value(3))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(1))
                .andExpect(jsonPath("$.message").value("Customers retrieved successfully"));
    }

    @Test
    void shouldFilterByFullNameContainingIgnoreCase() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("First Customer").email("first@customer.com").phone("111111111").build(), 0);
        perGetRequest(Map.of("fullName", "first"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fullName").value("First Customer"));
    }

    @Test
    void shouldFilterByEmailContainingIgnoreCase() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("First Customer").email("first@customer.com").phone("111111111").build(), 0);
        perGetRequest(Map.of("email", "first@"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("first@customer.com"));
    }

    @Test
    void shouldFilterByPhoneNumberContaining() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("First Customer").email("first@customer.com").phone("111111111").build(), 0);
        perGetRequest(Map.of("phoneNumber", "111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].phoneNumber").value("+34111111111"));
    }

    @Test
    void shouldCombineMultipleFilters() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("First Customer").email("first@customer.com").phone("111111111").build(), 0);
        perGetRequest(Map.of(
                "fullName", "first",
                "email", "first@",
                "phoneNumber", "11111"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].fullName").value("First Customer"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoCustomersMatch() throws Exception {
        perGetRequest(Map.of("fullName", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.page.totalElements").value(0))
                .andExpect(jsonPath("$.data.page.totalPages").value(0))
                .andExpect(jsonPath("$.message").value("Customers retrieved successfully"));
    }

    @Test
    void shouldRespectPaginationWithFilters() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("First Customer").email("first@customer.com").phone("111111111").build(), 0);
        performOrderForNewReservation(CustomerData.builder().name("Second Customer").email("second@customer.com").phone("222222222").build(), 1);

        perGetRequest(Map.of(
                "fullName", "First",
                "page", "0",
                "size", "1"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page.size").value(1))
                .andExpect(jsonPath("$.data.page.totalElements").value(1))
                .andExpect(jsonPath("$.data.page.totalPages").value(1))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.message").value("Customers retrieved successfully"));
    }

    @Test
    void shouldReturnCustomersSortedByCreationDateDesc() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("Old Customer").email("old@customer.com").phone("111111111").build(), 0);
        performOrderForNewReservation(CustomerData.builder().name("New Customer").email("new@customer.com").phone("222222222").build(), 1);

        perGetRequest(Map.of(
                "page", "0",
                "size", "2"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fullName").value("New Customer"))
                .andExpect(jsonPath("$.data.content[1].fullName").value("Old Customer"));
    }

    @Test
    void shouldPaginateMultiplePagesCorrectly() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("Customer A").email("a@customer.com").phone("111").build(), 0);
        performOrderForNewReservation(CustomerData.builder().name("Customer B").email("b@customer.com").phone("222").build(), 1);
        performOrderForNewReservation(CustomerData.builder().name("Customer C").email("c@customer.com").phone("333").build(), 2);

        perGetRequest(Map.of("page", "1", "size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(2))
                .andExpect(jsonPath("$.data.page.number").value(1));
    }

    @Test
    void shouldIgnoreUnknownQueryParameters() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("Ignored Param").email("ignored@customer.com").phone("999").build(), 0);

        perGetRequest(Map.of("nonexistentFilter", "something"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Ignored Param"));
    }

    @Test
    void shouldReturnAllCustomersWhenFiltersAreNullOrEmpty() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("Customer One").email("one@customer.com").phone("123").build(), 0);
        performOrderForNewReservation(CustomerData.builder().name("Customer Two").email("two@customer.com").phone("456").build(), 1);

        perGetRequest(new HashMap<>())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.totalElements").value(2));
    }

    @Test
    void shouldTrimWhitespaceInFilters() throws Exception {
        performOrderForNewReservation(CustomerData.builder().name("Whitespace User").email("white@space.com").phone("123456789").build(), 0);

        perGetRequest(Map.of("fullName", "   Whitespace   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fullName").value("Whitespace User"));
    }

    private ResultActions perGetRequest(Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder request = get(customersPath).header(AUTHORIZATION, "Bearer " + accessToken);

        if (params != null) {
            params.forEach(request::param);
        }
        return mockMvc.perform(request);
    }

    private void performOrderForNewReservation(CustomerData customerData, int ticketIndex) throws Exception {
        Cart cart = performReservationForNewCart(ticketIndex);
        CustomerCreate customer = CustomerCreate.builder()
                .fullName(customerData.name)
                .email(customerData.email)
                .phoneNumber(PhoneNumberData.builder()
                        .prefix("+34")
                        .nationalNumber(customerData.phone)
                        .build())
                .build();
        List<Long> cartTickets = ticketsRepository.findAllByCart(cart).stream().map(Ticket::getId).toList();
        createOrder(cart.getId(), cartTickets, customer);
    }

    private Cart performReservationForNewCart(int ticketIndex) throws Exception {
        Long cartId = createCart(associationId, accessToken);
        Long reservedTicket = ticketsRepository.findAllByRaffle(raffle).get(ticketIndex).getId();
        ReservationRequest request = ReservationRequest.builder().ticketsIds(List.of(reservedTicket)).build();
        performReserveRequest(request, associationId, cartId, accessToken).andReturn();
        return cartsRepository.findById(cartId).orElseThrow();
    }

    private void createOrder(Long cartId, List<Long> tickets, CustomerCreate customer) throws Exception {
        AdminOrderCreate orderRequest = new AdminOrderCreateBuilder()
                .withCartId(cartId)
                .withTicketIds(tickets)
                .withCustomer(customer)
                .build();
        performCreateOrderRequest(orderRequest, associationId, accessToken).andReturn();
    }

    @Builder
    private record CustomerData (
            String name,
            String email,
            String phone
    ) {}
}