package com.raffleease.raffleease.Domains.Notifications.Services;

import com.raffleease.raffleease.Domains.Notifications.Model.Notification;
import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface INotificationsService {
    Notification create(Order order);
}
