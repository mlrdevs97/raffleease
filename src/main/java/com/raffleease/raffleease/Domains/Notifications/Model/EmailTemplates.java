package com.raffleease.raffleease.Domains.Notifications.Model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EmailTemplates {
    ORDER_SUCCESS("order-success.html", "Order successfully completed");

    private final String template;
    private final String subject;
}