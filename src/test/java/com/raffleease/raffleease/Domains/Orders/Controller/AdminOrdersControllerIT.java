package com.raffleease.raffleease.Domains.Orders.Controller;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Orders.DTOs.AddCommentRequest;
import com.raffleease.raffleease.Domains.Orders.DTOs.AdminOrderCreate;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderComplete;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Orders.Model.OrderStatus;
import com.raffleease.raffleease.Domains.Orders.Repository.OrdersRepository;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Helpers.CustomerCreateBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.CLOSED;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderSource.ADMIN;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderStatus.CANCELLED;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderStatus.COMPLETED;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod.CARD;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod.CASH;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus.SUCCEEDED;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminOrdersControllerIT extends BaseIT {
    @Autowired
    protected OrdersRepository ordersRepository;
    protected Raffle raffle;
    protected Long raffleId;
    protected Long reservedTicket;
    protected Cart cart;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        Long raffleId = createRaffle(images, associationId, accessToken);
        raffle = rafflesRepository.findById(raffleId).orElseThrow();
        Long cartId = createCart(associationId, accessToken);
        cart = cartsRepository.findById(cartId).orElseThrow();
        List<Ticket> tickets = ticketsRepository.findAllByRaffle(raffle);
        reservedTicket = tickets.get(0).getId();
        ReservationRequest request = ReservationRequest.builder().ticketsIds(List.of(reservedTicket)).build();
        performReserveRequest(request, associationId, cartId, accessToken).andReturn();
        cart = cartsRepository.findById(cartId).orElseThrow();
    }

    @Test
    @Transactional
    void shouldCreateOrder() throws Exception {
        CustomerCreate customerRequest = new CustomerCreateBuilder().build();
        AdminOrderCreate request = createAdminOrder(cart.getId(), List.of(reservedTicket), customerRequest, null);

        MvcResult result = performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New order created successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.orderSource").value("ADMIN"))
                .andExpect(jsonPath("$.data.customer.fullName").value(customerRequest.fullName()))
                .andReturn();

        Long orderId = parseOrderId(result);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        assertThat(order).isNotNull();
        assertThat(order.getOrderSource()).isEqualTo(ADMIN);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(cart.getStatus()).isEqualTo(CLOSED);
        assertThat(cart.getTickets()).isNull();

        Payment payment = order.getPayment();
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getTotal()).isEqualByComparingTo(raffle.getTicketPrice());

        Customer customer = order.getCustomer();
        String requestPhoneNumber = customerRequest.phoneNumber().prefix() + customerRequest.phoneNumber().nationalNumber();
        assertThat(customer).isNotNull();
        assertThat(customer.getFullName()).isEqualTo(customerRequest.fullName());
        assertThat(customer.getEmail()).isEqualTo(customerRequest.email());
        assertThat(customer.getPhoneNumber()).isEqualTo(requestPhoneNumber);
    }

    @Test
    void shouldFailIfUserNotBelongToAssociation() throws Exception{
        String otherToken = registerOtherUser().accessToken();
        AdminOrderCreate request = createAdminOrder(cart.getId(), List.of(reservedTicket), new CustomerCreateBuilder().build(), null);
        performCreateOrderRequest(request, associationId, otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a member of this association"));
    }

    @Test
    void shouldFailIfCartIdIsMissing() throws Exception {
        AdminOrderCreate request = createAdminOrder(null, List.of(reservedTicket), new CustomerCreateBuilder().build(), null);

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cartId").value("Must provide cart id"));
    }

    @Test
    void shouldFailIfCartNotExist() throws Exception {
        Long nonExistentCartId = 999999L;
        AdminOrderCreate request = createAdminOrder(nonExistentCartId, List.of(reservedTicket), new CustomerCreateBuilder().build(), null);

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart not found for id <" + nonExistentCartId + ">"));
    }

    @Test
    void shouldFailIfNoTicketsAreProvided() throws Exception {
        AdminOrderCreate request = createAdminOrder(cart.getId(), List.of(), new CustomerCreateBuilder().build(), null);

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketIds").value("At least one ticket is required to create an order"));
    }

    @Test
    void shouldFailIfCommentExceedMaxLength() throws Exception {
        String longComment = "A".repeat(501);
        AdminOrderCreate request = createAdminOrder(null, List.of(reservedTicket), new CustomerCreateBuilder().build(), longComment);

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cartId").value("Must provide cart id"));
    }

    @Test
    void shouldFailIfCustomerNameIsMissing() throws Exception {
        CustomerCreate customer = new CustomerCreateBuilder().withFullName(null).build();
        AdminOrderCreate request = createAdminOrder(cart.getId(), List.of(reservedTicket), customer, null);

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['customer.fullName']").value("Must provide a name for user"));
    }

    @Test
    void shouldNotFailIfCustomerEmailIsMissing() throws Exception {
        CustomerCreate customer = new CustomerCreateBuilder().withEmail(null).build();
        AdminOrderCreate request = createAdminOrder(cart.getId(), List.of(reservedTicket), customer, null);

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New order created successfully"))
                .andExpect(jsonPath("$.data.customer.email").doesNotExist());
    }

    @Test
    void shouldNotFailIfCustomerPhoneIsMissing() throws Exception {
        CustomerCreate customer = new CustomerCreateBuilder().withPhoneNumber(null, null).build();
        AdminOrderCreate request = createAdminOrder(cart.getId(), List.of(reservedTicket), customer, null);

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New order created successfully"))
                .andExpect(jsonPath("$.data.customer.phoneNumber").doesNotExist());
    }

    @Test
    @Transactional
    void shouldFailIfSomeTicketsDoesNotBelongToCart() throws Exception {
        Long otherCartId = createCart(associationId, accessToken);
        List<Long> ticketIds = List.of(reservedTicket);

        AdminOrderCreate orderRequest = createAdminOrder(otherCartId, ticketIds, new CustomerCreateBuilder().build(), null);
        performCreateOrderRequest(orderRequest, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to current cart"));
    }

    @Test
    void shouldFailIfSomeTicketsDoesNotBelongToAssociation() throws Exception {
        AuthResponse authResponse = registerOtherUser();

        AdminOrderCreate orderRequest = createAdminOrder(cart.getId(), List.of(reservedTicket), new CustomerCreateBuilder().build(), null);
        performCreateOrderRequest(orderRequest, authResponse.association().id(), authResponse.accessToken())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to an association raffle"));
    }

    @Test
    void shouldFailIfAllTicketsAreNotIncludedInRequest() throws Exception {
        Long cartId = createCart(associationId, accessToken);
        List<Ticket> tickets = ticketsRepository.findAllByRaffle(raffle);
        List<Long> reserved = tickets.stream().map(Ticket::getId).toList();
        ReservationRequest request = ReservationRequest.builder().ticketsIds(reserved).build();
        performReserveRequest(request, associationId, cartId, accessToken).andReturn();

        AdminOrderCreate orderRequest = createAdminOrder(cartId, List.of(reservedTicket), new CustomerCreateBuilder().build(), null);
        performCreateOrderRequest(orderRequest, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to current cart"));
    }

    @Test
    @Transactional
    void shouldCompleteOrder() throws Exception {
        Long orderId = createOrder();
        OrderComplete completeRequest = new OrderComplete(CARD);
        performCompleteOrderRequest(orderId, completeRequest, associationId, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order completed successfully"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.payment.paymentMethod").value(CARD.getValue()));

        Order order = ordersRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(COMPLETED);
        assertThat(order.getCompletedAt()).isNotNull();
        assertThat(order.getPayment().getPaymentMethod()).isEqualTo(CARD);
        assertThat(order.getPayment().getStatus()).isEqualTo(SUCCEEDED);
        assertThat(order.getPayment().getCompletedAt()).isNotNull();

        List<Long> ticketIds = order.getOrderItems().stream().map(OrderItem::getTicketId).toList();
        List<Ticket> tickets = ticketsRepository.findAllById(ticketIds);
        tickets.forEach(ticket -> assertThat(ticket.getStatus()).isEqualTo(SOLD));
    }

    @Test
    @Transactional
    void shouldFailToCompleteIfOrderIsAlreadyCompleted() throws Exception {
        Long orderId = createOrder();
        updateOrderStatus(orderId, COMPLETED);
        OrderComplete completeRequest = new OrderComplete(CASH);
        performCompleteOrderRequest(orderId, completeRequest, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from COMPLETED to COMPLETED"));
    }

    @Test
    @Transactional
    void shouldFailToCompleteIfOrderIsCancelled() throws Exception {
        Long orderId = createOrder();
        updateOrderStatus(orderId, CANCELLED);
        OrderComplete completeRequest = new OrderComplete(CASH);
        performCompleteOrderRequest(orderId, completeRequest, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from CANCELLED to COMPLETED"));
    }

    @Test
    @Transactional
    void shouldCancelOrder() throws Exception {
        Long orderId = createOrder();
        performCancelOrderRequest(orderId, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        Order order = ordersRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(CANCELLED);
        assertThat(order.getCancelledAt()).isNotNull();
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(order.getPayment().getCancelledAt()).isNotNull();
    }

    @Test
    @Transactional
    void shouldFailToCancelIfOrderIsAlreadyCancelled() throws Exception {
        Long orderId = createOrder();
        updateOrderStatus(orderId, CANCELLED);
        performCancelOrderRequest(orderId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from CANCELLED to CANCELLED"));
    }

    @Test
    @Transactional
    void shouldFailToCancelIfOrderIsAlreadyCompleted() throws Exception {
        Long orderId = createOrder();
        updateOrderStatus(orderId, COMPLETED);
        performCancelOrderRequest(orderId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from COMPLETED to CANCELLED"));
    }

    @Test
    @Transactional
    void shouldAddCommentToPendingOrder() throws Exception {
        Long orderId = createOrder();

        AddCommentRequest commentRequest = AddCommentRequest.builder().comment("Delivered successfully.").build();
        performAddCommentRequest(orderId, commentRequest, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order comment added successfully"))
                .andExpect(jsonPath("$.data.comment").value("Delivered successfully."));

        Order order = ordersRepository.findById(orderId).orElseThrow();
        assertThat(order.getComment()).isEqualTo("Delivered successfully.");
    }

    @Test
    @Transactional
    void shouldAddCommentToCompletedOrder() throws Exception {
        Long orderId = createOrder();
        updateOrderStatus(orderId, COMPLETED);

        AddCommentRequest commentRequest = AddCommentRequest.builder().comment("Delivered successfully.").build();
        performAddCommentRequest(orderId, commentRequest, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order comment added successfully"))
                .andExpect(jsonPath("$.data.comment").value("Delivered successfully."));

        Order order = ordersRepository.findById(orderId).orElseThrow();
        assertThat(order.getComment()).isEqualTo("Delivered successfully.");
    }

    @Test
    void shouldFailToAddCommentIfTooLong() throws Exception {
        Long orderId = createOrder();
        String longComment = "a".repeat(501);

        AddCommentRequest commentRequest = AddCommentRequest.builder().comment(longComment).build();
        performAddCommentRequest(orderId, commentRequest, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comment").value("Comment must not exceed 500 characters"));
    }

    @Test
    void shouldFailToAddCommentIfNull() throws Exception {
        Long orderId = createOrder();

        AddCommentRequest commentRequest = AddCommentRequest.builder().comment(null).build();
        performAddCommentRequest(orderId, commentRequest, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comment").value("Comment cannot be null"));
    }

    @Test
    @Transactional
    void shouldReturnOrderById() throws Exception {
        Long orderId = createOrder();

        performGetOrderRequest(orderId, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    void shouldFailIfOrderNotFound() throws Exception {
        Long nonExistentOrderId = 999999L;

        performGetOrderRequest(nonExistentOrderId, accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found for id <" + nonExistentOrderId + ">"));
    }

    private Long createOrder() throws Exception {
        AdminOrderCreate request = createAdminOrder(cart.getId(), List.of(reservedTicket), new CustomerCreateBuilder().build(), null);
        return parseOrderId(performCreateOrderRequest(request, associationId, accessToken).andReturn());
    }

    private ResultActions performCreateOrderRequest(AdminOrderCreate request, Long associationId, String token) throws Exception {
        return mockMvc.perform(post("/admin/api/v1/associations/" + associationId + "/orders")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performCompleteOrderRequest(Long orderId, OrderComplete request, Long associationId, String token) throws Exception {
        return mockMvc.perform(put("/admin/api/v1/associations/" + associationId + "/orders/" + orderId + "/complete")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performCancelOrderRequest(Long orderId, String token) throws Exception {
        return mockMvc.perform(put("/admin/api/v1/associations/" + associationId + "/orders/" + orderId + "/cancel")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON));
    }


    private ResultActions performAddCommentRequest(Long orderId, AddCommentRequest request, String token) throws Exception {
        return mockMvc.perform(patch("/admin/api/v1/associations/" + associationId + "/orders/" + orderId + "/comment")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private AdminOrderCreate createAdminOrder(Long cartId, List<Long> tickets, CustomerCreate customer, String comment) {
        return AdminOrderCreate.builder()
                .cartId(cartId)
                .ticketIds(tickets)
                .customer(customer)
                .comment(comment)
                .build();
    }

    private Long parseOrderId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        ordersRepository.save(order);
    }

    private ResultActions performGetOrderRequest(Long orderId, String token) throws Exception {
        return mockMvc.perform(get("/admin/api/v1/associations/" + associationId + "/orders/" + orderId)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(APPLICATION_JSON));
    }
}