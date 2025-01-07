package com.raffleease.raffleease.Domains.Notifications.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import jakarta.mail.MessagingException;

public interface IEmailsService {
    void sendOrderSuccessNotification(Order order) throws MessagingException;
}
