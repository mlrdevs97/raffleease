package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Customers.DTO.CustomerCreate;
import com.raffleease.raffleease.Domains.Orders.DTOs.AdminOrderCreate;
import java.util.List;

public class AdminOrderCreateBuilder {
    private Long cartId = 1L;
    private List<Long> ticketIds = List.of(1L);
    private CustomerCreate customer = new CustomerCreateBuilder().build();
    private String comment = null;

    public AdminOrderCreateBuilder withCartId(Long cartId) {
        this.cartId = cartId;
        return this;
    }

    public AdminOrderCreateBuilder withTicketIds(List<Long> ticketIds) {
        this.ticketIds = ticketIds;
        return this;
    }

    public AdminOrderCreateBuilder withCustomer(CustomerCreate customer) {
        this.customer = customer;
        return this;
    }

    public AdminOrderCreateBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public AdminOrderCreate build() {
        return AdminOrderCreate.builder()
                .cartId(cartId)
                .ticketIds(ticketIds)
                .customer(customer)
                .comment(comment)
                .build();
    }
}