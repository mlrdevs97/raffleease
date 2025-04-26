package com.raffleease.raffleease.Domains.Notifications.Services;

import com.raffleease.raffleease.Domains.Notifications.Model.Notification;
import com.raffleease.raffleease.Domains.Notifications.Model.NotificationChannel;
import com.raffleease.raffleease.Domains.Notifications.Model.NotificationType;
import com.raffleease.raffleease.Domains.Orders.Model.Order;

public interface NotificationsService {
    Notification create(NotificationType notificationType, NotificationChannel channel);
}
