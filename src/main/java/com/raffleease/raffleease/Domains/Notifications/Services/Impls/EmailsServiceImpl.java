package com.raffleease.raffleease.Domains.Notifications.Services.Impls;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Notifications.Model.NotificationType;
import com.raffleease.raffleease.Domains.Notifications.Services.EmailsService;
import com.raffleease.raffleease.Domains.Notifications.Services.NotificationsService;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.CustomMailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Notifications.Model.EmailTemplate.EMAIL_VERIFICATION;
import static com.raffleease.raffleease.Domains.Notifications.Model.EmailTemplate.ORDER_SUCCESS;
import static com.raffleease.raffleease.Domains.Notifications.Model.EmailTemplate.PASSWORD_RESET;
import static com.raffleease.raffleease.Domains.Notifications.Model.NotificationChannel.EMAIL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailsServiceImpl implements EmailsService {
    private final NotificationsService notificationsService;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${MAIL_USERNAME}")
    private String userName;

    @Override
    @Async
    public void sendEmailVerificationEmail(User user, String link) {
        Map<String, Object> variables = createEmailVerificationEmailVariables(user, link);
        sendEmail(user.getEmail(), EMAIL_VERIFICATION.getSubject(), EMAIL_VERIFICATION.getTemplate(), variables);
        notificationsService.create(NotificationType.EMAIL_VERIFICATION, EMAIL);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String link) {
        Map<String, Object> variables = createPasswordResetEmailVariables(user, link);
        sendEmail(user.getEmail(), PASSWORD_RESET.getSubject(), PASSWORD_RESET.getTemplate(), variables);
        notificationsService.create(NotificationType.PASSWORD_RESET, EMAIL);
    }

    @Override
    @Async
    public void sendOrderSuccessEmail(Order order) {
        Map<String, Object> variables = createOrderSuccessEmailVariables(order);
        sendEmail(order.getCustomer().getEmail(), ORDER_SUCCESS.getSubject(), ORDER_SUCCESS.getTemplate(), variables);
        notificationsService.create(NotificationType.ORDER_SUCCESS, EMAIL);
    }

    private Map<String, Object> createEmailVerificationEmailVariables(User user, String link) {
        Map<String, Object> variables = new HashMap<>();
        String formattedRegistrationDate = user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));

        variables.put("customerName", user.getUserName());
        variables.put("customerEmail", user.getEmail());
        variables.put("registrationDate", formattedRegistrationDate);
        variables.put("verificationUrl", link);

        return variables;
    }

    private Map<String, Object> createPasswordResetEmailVariables(User user, String link) {
        Map<String, Object> variables = new HashMap<>();
        String formattedRequestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));

        variables.put("customerName", user.getUserName());
        variables.put("customerEmail", user.getEmail());
        variables.put("requestDate", formattedRequestDate);
        variables.put("resetUrl", link);

        return variables;
    }

    private Map<String, Object> createOrderSuccessEmailVariables(Order order) {
        Map<String, Object> variables = new HashMap<>();
        Payment paymentData = order.getPayment();
        Customer customer = order.getCustomer();
        String formattedOrderDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));
        List<String> ticketNumbers = order.getOrderItems().stream()
                .map(OrderItem::getTicketNumber)
                .collect(Collectors.toList());

        variables.put("customer", customer);
        variables.put("paymentData", paymentData);
        variables.put("orderData", order);
        variables.put("customerName", customer.getFullName());
        variables.put("customerEmail", customer.getEmail());
        variables.put("customerPhoneNumber", customer.getPhoneNumber());
        variables.put("paymentMethod", paymentData.getPaymentMethod());
        variables.put("paymentTotal", paymentData.getTotal());
        variables.put("orderReference", order.getOrderReference());
        variables.put("createdAt", formattedOrderDate);
        variables.put("ticketList", ticketNumbers);
        variables.put("ticketCount", ticketNumbers.size());

        return variables;
    }

    private void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED_RELATED, UTF_8.name());
            Context context = new Context();
            context.setVariables(variables);
            String htmlTemplate = templateEngine.process(templateName, context);

            messageHelper.setSubject(subject);
            messageHelper.setText(htmlTemplate, true);
            messageHelper.setTo(to);
            messageHelper.setFrom(userName);

            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            throw new CustomMailException("Error occurred while sending notification: " + ex.getMessage());
        }
    }
}
