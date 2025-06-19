package com.raffleease.raffleease.Domains.Notifications.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface EmailsService {
    void sendOrderSuccessEmail(Order order);
    void sendEmailVerificationEmail(User user, String link);
    void sendPasswordResetEmail(User user, String link);
}
