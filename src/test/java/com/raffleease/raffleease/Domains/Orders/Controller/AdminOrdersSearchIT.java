package com.raffleease.raffleease.Domains.Orders.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Orders.Repository.OrderItemsRepository;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus;
import com.raffleease.raffleease.Domains.Payments.Repository.PaymentsRepository;
import com.raffleease.raffleease.Helpers.CustomerBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.raffleease.raffleease.Domains.Orders.Model.OrderSource.CUSTOMER;
import static com.raffleease.raffleease.Domains.Orders.Model.OrderStatus.*;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod.CARD;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentMethod.CASH;
import static com.raffleease.raffleease.Domains.Payments.Model.PaymentStatus.SUCCEEDED;
import static java.math.BigDecimal.ZERO;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminOrdersSearchIT extends BaseAminOrdersIT {
    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Test
    void shouldReturnAllOrdersWhenNoFiltersProvided() throws Exception {
        final int numOrders = 3;
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < numOrders; i++) {
            createAndReserveTicketsForCart(associationId, accessToken);
            ids.add(createOrder(associationId, accessToken));
        }

        performSearchRequest(associationId, accessToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(ordersRepository.findAll().size()));
    }

    @Test
    void shouldFilterByOrderStatus() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        Long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.setStatus(COMPLETED);
        ordersRepository.save(order);
        Map<String, String> filters = Map.of("status", COMPLETED.toString());
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByPaymentStatus() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        Long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        Payment payment = paymentsRepository.findByOrder(order).orElseThrow();
        payment.setStatus(SUCCEEDED);
        paymentsRepository.save(payment);
        Map<String, String> filters = Map.of("paymentStatus", SUCCEEDED.toString());
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByPaymentMethod() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        Long orderId1 = createOrder(associationId, accessToken);
        Order order1 = ordersRepository.findById(orderId1).orElseThrow();
        Payment payment1 = paymentsRepository.findByOrder(order1).orElseThrow();
        payment1.setPaymentMethod(CARD);
        paymentsRepository.save(payment1);

        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId2 = createOrder(associationId, accessToken);
        Order order2 = ordersRepository.findById(orderId2).orElseThrow();
        Payment payment2 = paymentsRepository.findByOrder(order2).orElseThrow();
        payment2.setPaymentMethod(CASH);
        paymentsRepository.save(payment2);

        Map<String, String> filters = Map.of("paymentMethod", CASH.toString());
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    // TODO: Enhance order source testing when customer order source is implemented
    @Test
    void shouldFilterByOrderSource() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        Order order = ordersRepository.save(Order.builder()
                .association(associationsRepository.findById(associationId).orElseThrow())
                .orderReference("example-order-reference")
                .orderSource(CUSTOMER)
                .status(PENDING)
                .customer(new CustomerBuilder().build())
                .build());

        OrderItem orderItem = OrderItem.builder()
                .ticketNumber("12345")
                .priceAtPurchase(new BigDecimal("15.50"))
                .ticketId(reservedTicket)
                .raffleId(raffleId)
                .order(order)
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        Payment payment = paymentsRepository.save(Payment.builder()
                .order(order)
                .total(ZERO)
                .status(PaymentStatus.PENDING)
                .build());

        order.setPayment(payment);
        order.setOrderItems(items);
        ordersRepository.save(order);

        Map<String, String> filters = Map.of("orderSource", CUSTOMER.toString());
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByOrderReferenceContainingIgnoreCase() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        Map<String, String> filters = Map.of("orderReference", order.getOrderReference());
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByCustomerNameContainingIgnoreCase() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.getCustomer().setFullName("Test Name For Other User");
        ordersRepository.save(order);
        String nameContaining = order.getCustomer().getFullName().toLowerCase().substring(5, 19);
        Map<String, String> filters = Map.of("customerName", nameContaining);
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByCustomerEmailContainingIgnoreCase() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.getCustomer().setEmail("UNIQUE-test-email@user.com");
        ordersRepository.save(order);
        String emailContaining = order.getCustomer().getEmail().toLowerCase().substring(0, 16);
        Map<String, String> filters = Map.of("customerEmail", emailContaining);
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByCustomerPhoneContaining() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.getCustomer().setPhoneNumber("987987987");
        ordersRepository.save(order);
        String phoneContaining = order.getCustomer().getPhoneNumber().substring(0, 7);
        Map<String, String> filters = Map.of("customerPhone", phoneContaining);
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByRaffleId() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        OrderItem item = OrderItem.builder()
                .ticketNumber("12345")
                .priceAtPurchase(new BigDecimal("15.50"))
                .ticketId(reservedTicket)
                .raffleId(1234L)
                .order(order)
                .build();
        List<OrderItem> orderItems = orderItemsRepository.findByOrder(order);
        orderItems.add(item);
        order.setOrderItems(orderItems);
        ordersRepository.save(order);
        Map<String, String> filters = Map.of("raffleId", item.getRaffleId().toString());
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByMinAndMaxTotal() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId1 = createOrder(associationId, accessToken);
        Order order1 = ordersRepository.findById(orderId1).orElseThrow();
        Payment payment1 = paymentsRepository.findByOrder(order1).orElseThrow();
        payment1.setTotal(new BigDecimal("15.00"));
        paymentsRepository.save(payment1);

        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId2 = createOrder(associationId, accessToken);
        Order order2 = ordersRepository.findById(orderId2).orElseThrow();
        Payment payment2 = paymentsRepository.findByOrder(order2).orElseThrow();
        payment2.setTotal(new BigDecimal("30.00"));
        paymentsRepository.save(payment2);

        Map<String, String> filters = Map.of("minTotal", "20", "maxTotal", "35");
        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByCreatedAtRange() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.setCreatedAt(LocalDateTime.now().minusDays(3));
        ordersRepository.save(order);

        Map<String, String> filters = Map.of(
                "createdAtFrom", LocalDateTime.now().minusDays(4).toString(),
                "createdAtTo", LocalDateTime.now().minusDays(2).toString()
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByCompletedAtRange() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.setStatus(COMPLETED);
        order.setCompletedAt(LocalDateTime.now().minusDays(2));
        ordersRepository.save(order);

        Map<String, String> filters = Map.of(
                "completedAtFrom", LocalDateTime.now().minusDays(3).toString(),
                "completedAtTo", LocalDateTime.now().minusDays(1).toString()
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldFilterByCancelledAtRange() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.setStatus(CANCELLED);
        order.setCancelledAt(LocalDateTime.now().minusDays(1));
        ordersRepository.save(order);

        Map<String, String> filters = Map.of(
                "cancelledAtFrom", LocalDateTime.now().minusDays(2).toString(),
                "cancelledAtTo", LocalDateTime.now().toString()
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldReturnOrdersMatchingMultipleFilters() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.setStatus(COMPLETED);
        order.setCompletedAt(LocalDateTime.now().minusDays(1));
        order.getCustomer().setEmail("filter-matching@test.com");
        ordersRepository.save(order);

        Payment payment = paymentsRepository.findByOrder(order).orElseThrow();
        payment.setPaymentMethod(CARD);
        payment.setTotal(new BigDecimal("25.00"));
        paymentsRepository.save(payment);

        Map<String, String> filters = Map.of(
                "status", COMPLETED.toString(),
                "paymentMethod", CARD.toString(),
                "minTotal", "20",
                "maxTotal", "30",
                "completedAtFrom", LocalDateTime.now().minusDays(2).toString(),
                "completedAtTo", LocalDateTime.now().toString(),
                "customerEmail", "filter-matching"
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldReturn400WhenInvalidEnumValueProvided() throws Exception {
        Map<String, String> filters = Map.of("paymentMethod", "INVALID_ENUM");

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.paymentMethod").value("Invalid value 'INVALID_ENUM' for parameter 'paymentMethod'"));
    }

    @Test
    void shouldFailWhenCustomerNameExceedsMaxLength() throws Exception {
        String longName = "A".repeat(101);
        Map<String, String> filters = Map.of("customerName", longName);

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerName").value("Customer name must not exceed 100 characters"));
    }

    @Test
    void shouldFailWhenCustomerEmailExceedsMaxLength() throws Exception {
        String longEmail = "a".repeat(100) + "@test.com";
        Map<String, String> filters = Map.of("customerEmail", longEmail);

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerEmail").value("Customer email must not exceed 100 characters"));
    }

    @Test
    void shouldFailWhenCustomerPhoneExceedsMaxLength() throws Exception {
        String longPhone = "9".repeat(31);
        Map<String, String> filters = Map.of("customerPhone", longPhone);

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerPhone").value("Phone number must not exceed 30 characters"));
    }

    @Test
    void shouldFailWhenRaffleIdIsNegativeOrZero() throws Exception {
        Map<String, String> filters = Map.of("raffleId", "-1");

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.raffleId").value("Raffle ID must be a positive number"));
    }

    @Test
    void shouldFailWhenMinTotalIsNegative() throws Exception {
        Map<String, String> filters = Map.of("minTotal", "-10");

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.minTotal").value("Minimum total must be 0 or more"));
    }

    @Test
    void shouldFailWhenMaxTotalIsNegative() throws Exception {
        Map<String, String> filters = Map.of("maxTotal", "-5");

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.maxTotal").value("Maximum total must be 0 or more"));
    }

    @Test
    void shouldFailWhenMinTotalIsGreaterThanMaxTotal() throws Exception {
        Map<String, String> filters = Map.of("minTotal", "50", "maxTotal", "10");

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.minTotal").value("Minimum total cannot be greater than maximum total"));
    }

    @Test
    void shouldFailWhenCreatedFromIsAfterCreatedTo() throws Exception {
        Map<String, String> filters = Map.of(
                "createdFrom", LocalDateTime.now().plusDays(1).toString(),
                "createdTo", LocalDateTime.now().toString()
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.createdFrom").value("createdFrom must be before or equal to createdTo"));
    }

    @Test
    void shouldFailWhenCompletedFromIsAfterCompletedTo() throws Exception {
        Map<String, String> filters = Map.of(
                "completedFrom", LocalDateTime.now().plusDays(1).toString(),
                "completedTo", LocalDateTime.now().toString()
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.completedFrom").value("completedFrom must be before or equal to completedTo"));
    }

    @Test
    void shouldFailWhenCancelledFromIsAfterCancelledTo() throws Exception {
        Map<String, String> filters = Map.of(
                "cancelledFrom", LocalDateTime.now().plusDays(1).toString(),
                "cancelledTo", LocalDateTime.now().toString()
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cancelledFrom").value("cancelledFrom must be before or equal to cancelledTo"));
    }


    @Test
    void shouldReturnPagedResults() throws Exception {
        for (int i = 0; i < 5; i++) {
            createAndReserveTicketsForCart(associationId, accessToken);
            createOrder(associationId, accessToken);
        }

        performSearchRequest(associationId, accessToken, Map.of("page", "0", "size", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(4))
                .andExpect(jsonPath("$.data.page.size").value(4))
                .andExpect(jsonPath("$.data.page.totalElements").value(5))
                .andExpect(jsonPath("$.data.page.totalPages").value(2));
    }

    @Test
    void shouldReturnSortedByCreatedAtDescByDefault() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        createOrder(associationId, accessToken);
        createAndReserveTicketsForCart(associationId, accessToken);
        long secondOrderId = createOrder(associationId, accessToken);

        performSearchRequest(associationId, accessToken, Map.of("page", "0", "size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(secondOrderId));
    }

    @Test
    void shouldReturnCorrectSecondPage() throws Exception {
        for (int i = 0; i < 5; i++) {
            createAndReserveTicketsForCart(associationId, accessToken);
            createOrder(associationId, accessToken);
        }

        performSearchRequest(associationId, accessToken, Map.of("page", "1", "size", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldReturnEmptyPageIfOutOfBounds() throws Exception {
        for (int i = 0; i < 3; i++) {
            createAndReserveTicketsForCart(associationId, accessToken);
            createOrder(associationId, accessToken);
        }

        performSearchRequest(associationId, accessToken, Map.of("page", "5", "size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    void shouldRespectCustomPageSize() throws Exception {
        for (int i = 0; i < 4; i++) {
            createAndReserveTicketsForCart(associationId, accessToken);
            createOrder(associationId, accessToken);
        }

        performSearchRequest(associationId, accessToken, Map.of("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void shouldNotReturnOrdersFromOtherAssociations() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        Long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        AuthResponse otherAuth = registerOtherUser();
        Map<String, String> filters = Map.of("orderReference", order.getOrderReference());
        performSearchRequest(otherAuth.associationId(), otherAuth.accessToken(), filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void shouldTrimOrderSearchFilters() throws Exception {
        createAndReserveTicketsForCart(associationId, accessToken);
        long orderId = createOrder(associationId, accessToken);
        Order order = ordersRepository.findById(orderId).orElseThrow();
        order.setOrderReference("TRIM-REF-001");
        order.getCustomer().setFullName("Trimmed Customer");
        order.getCustomer().setEmail("trim@example.com");
        order.getCustomer().setPhoneNumber("+34123123123");
        ordersRepository.save(order);

        Map<String, String> filters = Map.of(
                "orderReference", "  TRIM-REF-001  ",
                "customerName", "  Trimmed  ",
                "customerEmail", "  trim@example.com ",
                "customerPhone", "  +34123123123 "
        );

        performSearchRequest(associationId, accessToken, filters)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].orderReference").value("TRIM-REF-001"))
                .andExpect(jsonPath("$.data.content[0].customer.fullName").value("Trimmed Customer"));
    }

    private ResultActions performSearchRequest(Long associationId, String accessToken, Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/admin/api/v1/associations/" + associationId + "/orders")
                .header(AUTHORIZATION, "Bearer " + accessToken);

        if (params != null) {
            params.forEach(requestBuilder::param);
        }
        return mockMvc.perform(requestBuilder);
    }
}
