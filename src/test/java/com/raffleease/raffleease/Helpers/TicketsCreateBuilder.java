package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import java.math.BigDecimal;

public class TicketsCreateBuilder {
    private Long amount = 5L;
    private BigDecimal price = BigDecimal.valueOf(1.50);
    private Long lowerLimit = 100L;

    public TicketsCreateBuilder withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public TicketsCreateBuilder withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public TicketsCreateBuilder withLowerLimit(Long lowerLimit) {
        this.lowerLimit = lowerLimit;
        return this;
    }

    public TicketsCreate build() {
        return new TicketsCreate(amount, price, lowerLimit);
    }
}
