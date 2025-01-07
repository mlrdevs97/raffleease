package com.raffleease.raffleease.Domains.Notifications.Services.Impls;

import com.raffleease.raffleease.Domains.Notifications.Model.Notification;
import com.raffleease.raffleease.Domains.Notifications.Repository.INotificationsRepository;
import com.raffleease.raffleease.Domains.Notifications.Services.IEmailsService;
import com.raffleease.raffleease.Domains.Notifications.Services.INotificationsService;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Exceptions.CustomExceptions.CustomMailException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.raffleease.raffleease.Domains.Notifications.Model.NotificationType.ORDER_SUCCESS;

@RequiredArgsConstructor
@Service
public class NotificationsServiceImpl implements INotificationsService {
    private final INotificationsRepository repository;
    private final IEmailsService emailsService;

    @Transactional
    @Override
    public Notification create(Order order) {
        Notification notification = Notification.builder()
                .notificationDate(LocalDateTime.now())
                .notificationType(ORDER_SUCCESS)
                .order(order)
                .build();

        try {
            emailsService.sendOrderSuccessNotification(order);
        } catch (MessagingException ex) {
            throw new CustomMailException("Error occurred while sending notification: " + ex.getMessage());
        }
        return save(notification);
    }

    public Notification save(Notification entity) {
        try {
            return repository.save(entity);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving entity: " + ex.getMessage());
        }
    }
}
