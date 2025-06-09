package com.raffleease.raffleease.Domains.Notifications.Services;

import com.raffleease.raffleease.Domains.Notifications.Model.Notification;
import com.raffleease.raffleease.Domains.Notifications.Model.NotificationChannel;
import com.raffleease.raffleease.Domains.Notifications.Model.NotificationType;

public interface NotificationsService {
    Notification create(NotificationType notificationType, NotificationChannel channel);
}
