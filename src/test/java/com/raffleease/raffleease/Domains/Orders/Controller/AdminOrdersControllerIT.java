package com.raffleease.raffleease.Domains.Orders.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Carts.DTO.ReservationRequest;
import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Orders.DTOs.AddCommentRequest;
import com.raffleease.raffleease.Domains.Orders.DTOs.AdminOrderCreate;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderComplete;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Orders.Model.OrderStatus;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Helpers.AdminOrderCreateBuilder;
import com.raffleease.raffleease.Helpers.CustomerCreateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.CLOSED;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderSource.ADMIN;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderStatus.CANCELLED;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderStatus.COMPLETED;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod.CARD;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod.CASH;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus.SUCCEEDED;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.RESERVED;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminOrdersControllerIT extends BaseAminOrdersIT {
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        createAndReserveTicketsForCart(associationId, accessToken, 0);
    }

    @Test
    void shouldCreateOrder() throws Exception {
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .build();

        MvcResult result = performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New order created successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.orderSource").value("ADMIN"))
                .andExpect(jsonPath("$.data.customer.fullName").value(request.customer().fullName()))
                .andReturn();

        Long orderId = parseOrderId(result);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        assertThat(order).isNotNull();
        assertThat(order.getOrderSource()).isEqualTo(ADMIN);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        cart = cartsRepository.findById(cart.getId()).orElseThrow();
        assertThat(cart.getStatus()).isEqualTo(CLOSED);
        assertThat(ticketsRepository.findAllByCart(cart).isEmpty()).isTrue();
        assertThat(ticketsRepository.findAllByCart(cart).isEmpty()).isTrue();

        Ticket ticket = ticketsRepository.findById(reservedTicket).orElseThrow();
        assertThat(ticket).isNotNull();
        assertThat(ticket.getCart()).isNull();
        assertThat(ticket.getStatus()).isEqualTo(RESERVED);

        List<OrderItem> items = itemsRepository.findByOrder(order);
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).getTicketId()).isEqualTo(reservedTicket);

        Payment payment = order.getPayment();
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getTotal()).isEqualByComparingTo(raffle.getTicketPrice());

        Customer customer = order.getCustomer();
        String requestPhoneNumber = request.customer().phoneNumber().prefix() + request.customer().phoneNumber().nationalNumber();
        assertThat(customer).isNotNull();
        assertThat(customer.getFullName()).isEqualTo(request.customer().fullName());
        assertThat(customer.getEmail()).isEqualTo(request.customer().email());
        assertThat(customer.getPhoneNumber()).isEqualTo(requestPhoneNumber);
    }

    @Test
    void shouldTrimAndNormalizeCustomerDataAndCommentOnOrderCreate() throws Exception {
        CustomerCreateBuilder customerBuilder = new CustomerCreateBuilder();
        String rawFullName = "  " + customerBuilder.getFullName() + "  ";
        String rawEmail = "  " + customerBuilder.getEmail().toUpperCase() + "  ";
        String rawPrefix = "  " + customerBuilder.getUserPhonePrefix() + " ";
        String rawNumber = " " + customerBuilder.getUserPhoneNumber() + "  ";

        String rawComment = "   Urgent delivery.  ";

        CustomerCreate customer = new CustomerCreateBuilder()
                .withFullName(rawFullName)
                .withEmail(rawEmail)
                .withPhoneNumber(rawPrefix, rawNumber)
                .build();

        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .withCustomer(customer)
                .withComment(rawComment)
                .build();

        MvcResult result = performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New order created successfully"))
                .andReturn();

        Long orderId = parseOrderId(result);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        Customer savedCustomer = order.getCustomer();

        assertThat(savedCustomer.getFullName()).isEqualTo(customerBuilder.getFullName());
        assertThat(savedCustomer.getEmail()).isEqualTo(customerBuilder.getEmail().toLowerCase());
        assertThat(savedCustomer.getPhoneNumber()).isEqualTo(
                customerBuilder.getUserPhonePrefix() + customerBuilder.getUserPhoneNumber()
        );
        assertThat(order.getComment()).isEqualTo(rawComment.trim());
    }

    @Test
    void shouldFailIfUserNotBelongToAssociation() throws Exception{
        String otherToken = registerOtherUser().accessToken();
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .build();
        performCreateOrderRequest(request, associationId, otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a member of this association"));
    }

    @Test
    void shouldFailIfCartIdIsMissing() throws Exception {
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(null)
                .withTicketIds(List.of(reservedTicket))
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cartId").value("REQUIRED"));
    }

    @Test
    void shouldFailIfCartNotExist() throws Exception {
        Long nonExistentCartId = 999999L;
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(nonExistentCartId)
                .withTicketIds(List.of(nonExistentCartId))
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart not found for id <" + nonExistentCartId + ">"));
    }

    @Test
    void shouldFailIfNoTicketsAreProvided() throws Exception {
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of())
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketIds").value("REQUIRED"));
    }

    @Test
    void shouldFailIfCommentExceedMaxLength() throws Exception {
        String longComment = "A".repeat(501);
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .withComment(longComment)
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comment").value("INVALID_LENGTH"));
    }

    @Test
    void shouldFailIfCustomerNameIsMissing() throws Exception {
        CustomerCreate customer = new CustomerCreateBuilder().withFullName(null).build();
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .withCustomer(customer)
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['customer.fullName']").value("REQUIRED"));
    }

    @Test
    void shouldNotFailIfCustomerEmailIsMissing() throws Exception {
        CustomerCreate customer = new CustomerCreateBuilder().withEmail(null).build();
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .withCustomer(customer)
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New order created successfully"))
                .andExpect(jsonPath("$.data.customer.email").doesNotExist());
    }

    @Test
    void shouldNotFailIfCustomerPhoneIsMissing() throws Exception {
        CustomerCreate customer = new CustomerCreateBuilder().withPhoneNumber(null, null).build();
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .withCustomer(customer)
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New order created successfully"))
                .andExpect(jsonPath("$.data.customer.phoneNumber").doesNotExist());
    }

    @Test
    void shouldFailIfSomeTicketsDoesNotBelongToCart() throws Exception {
        Long otherCartId = createCart(associationId, accessToken);
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(otherCartId)
                .withTicketIds(List.of(reservedTicket))
                .build();

        performCreateOrderRequest(request, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to current cart"));
    }

    @Test
    void shouldFailIfSomeTicketsDoesNotBelongToAssociation() throws Exception {
        AuthResponse authResponse = registerOtherUser();
        AdminOrderCreate request = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(List.of(reservedTicket))
                .build();

        performCreateOrderRequest(request, authResponse.associationId(), authResponse.accessToken())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to an association raffle"));
    }

    @Test
    void shouldFailIfAllTicketsAreNotIncludedInRequest() throws Exception {
        Long cartId = createCart(associationId, accessToken);
        List<Ticket> tickets = ticketsRepository.findAllByRaffle(raffle);
        List<Long> originalCartTickets = ticketsRepository.findAllByCart(cart).stream().map(Ticket::getId).toList();
        Set<Long> originalCartTicketIds = new HashSet<>(originalCartTickets);
        List<Long> reserved = tickets.stream()
                .map(Ticket::getId)
                .filter(ticketId -> !originalCartTicketIds.contains(ticketId)).toList();
        ReservationRequest request = ReservationRequest.builder().ticketsIds(reserved).build();
        performReserveRequest(request, associationId, cartId, accessToken).andReturn();
        AdminOrderCreate orderRequest = new AdminOrderCreateBuilder()
                .withCartId(cart.getId())
                .withTicketIds(reserved)
                .build();
        performCreateOrderRequest(orderRequest, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some tickets do not belong to current cart"));
    }

    @Test
    void shouldCompleteOrder() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
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

        List<OrderItem> orderItems = itemsRepository.findByOrder(order);
        List<Long> ticketIds = orderItems.stream().map(OrderItem::getTicketId).toList();
        List<Ticket> tickets = ticketsRepository.findAllById(ticketIds);
        tickets.forEach(ticket -> assertThat(ticket.getStatus()).isEqualTo(SOLD));
    }

    @Test
    void shouldFailToCompleteIfOrderIsAlreadyCompleted() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
        updateOrderStatus(orderId, COMPLETED);
        OrderComplete completeRequest = new OrderComplete(CASH);
        performCompleteOrderRequest(orderId, completeRequest, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from COMPLETED to COMPLETED"));
    }

    @Test
    void shouldFailToCompleteIfOrderIsCancelled() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
        updateOrderStatus(orderId, CANCELLED);
        OrderComplete completeRequest = new OrderComplete(CASH);
        performCompleteOrderRequest(orderId, completeRequest, associationId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from CANCELLED to COMPLETED"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
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
    void shouldFailToCancelIfOrderIsAlreadyCancelled() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
        updateOrderStatus(orderId, CANCELLED);
        performCancelOrderRequest(orderId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from CANCELLED to CANCELLED"));
    }

    @Test
    void shouldFailToCancelIfOrderIsAlreadyCompleted() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
        updateOrderStatus(orderId, COMPLETED);
        performCancelOrderRequest(orderId, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported status transition from COMPLETED to CANCELLED"));
    }

    @Test
    void shouldAddCommentToPendingOrder() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
        AddCommentRequest commentRequest = AddCommentRequest.builder().comment("Delivered successfully.").build();
        performAddCommentRequest(orderId, commentRequest, accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order comment added successfully"))
                .andExpect(jsonPath("$.data.comment").value("Delivered successfully."));

        Order order = ordersRepository.findById(orderId).orElseThrow();
        assertThat(order.getComment()).isEqualTo("Delivered successfully.");
    }

    @Test
    void shouldAddCommentToCompletedOrder() throws Exception {
        Long orderId = createOrder(associationId, accessToken);
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
        Long orderId = createOrder(associationId, accessToken);
        String longComment = "a".repeat(501);

        AddCommentRequest commentRequest = AddCommentRequest.builder().comment(longComment).build();
        performAddCommentRequest(orderId, commentRequest, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comment").value("INVALID_LENGTH"));
    }

    @Test
    void shouldFailToAddCommentIfNull() throws Exception {
        Long orderId = createOrder(associationId, accessToken);

        AddCommentRequest commentRequest = AddCommentRequest.builder().comment(null).build();
        performAddCommentRequest(orderId, commentRequest, accessToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comment").value("REQUIRED"));
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        Long orderId = createOrder(associationId, accessToken);

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