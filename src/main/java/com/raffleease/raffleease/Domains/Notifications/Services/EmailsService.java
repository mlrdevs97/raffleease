package com.raffleease.raffleease.Domains.Notifications.Services;

import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface EmailsService {
    /**
     * Sends an email to the user when an order is successfully created.
     * 
     * @param order the order to send the email to
     */
    void sendOrderSuccessEmail(Order order);

    /**
     * Sends an email to the user to verify their email address.
     * 
     * @param user the user to send the email to
     * @param link the link to verify the email address
     */
    void sendEmailVerificationEmail(User user, String link);

    /**
     * Sends an email to the user to reset their password.
     * 
     * @param user the user to send the email to
     * @param link the link to reset the password
     */
    void sendPasswordResetEmail(User user, String link);
}
