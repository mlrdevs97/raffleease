package com.raffleease.raffleease.Domains.Notifications.Services.Impls;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Notifications.Model.NotificationType;
import com.raffleease.raffleease.Domains.Notifications.Model.MailRequest;
import com.raffleease.raffleease.Domains.Notifications.Model.MailResponse;
import com.raffleease.raffleease.Domains.Notifications.Services.EmailsService;
import com.raffleease.raffleease.Domains.Notifications.Services.NotificationsService;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Orders.Model.OrderItem;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.CustomMailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
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
import static com.raffleease.raffleease.Domains.Notifications.Model.EmailTemplate.EMAIL_UPDATE_VERIFICATION;
import static com.raffleease.raffleease.Domains.Notifications.Model.NotificationChannel.EMAIL;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailsServiceImpl implements EmailsService {
    private final NotificationsService notificationsService;
    private final SpringTemplateEngine templateEngine;
    private final RestTemplate restTemplate;

    @Value("${mail.API-KEY}")
    private String apiKey;

    @Value("${mail.API-URL}")
    private String apiUrl;

    @Value("${mail.SENDER-EMAIL}")
    private String senderEmail;

    @Override
    @Async
    public void sendEmailVerificationEmail(User user, String link) {
        Map<String, Object> variables = createEmailVerificationEmailVariables(user, link);
        String htmlContent = processTemplate(EMAIL_VERIFICATION.getTemplate(), variables);
        sendEmail(user.getEmail(), EMAIL_VERIFICATION.getSubject(), htmlContent);
        notificationsService.create(NotificationType.EMAIL_VERIFICATION, EMAIL);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String link) {
        Map<String, Object> variables = createPasswordResetEmailVariables(user, link);
        String htmlContent = processTemplate(PASSWORD_RESET.getTemplate(), variables);
        sendEmail(user.getEmail(), PASSWORD_RESET.getSubject(), htmlContent);
        notificationsService.create(NotificationType.PASSWORD_RESET, EMAIL);
    }

    @Override
    @Async
    public void sendOrderSuccessEmail(Order order) {
        Map<String, Object> variables = createOrderSuccessEmailVariables(order);
        String htmlContent = processTemplate(ORDER_SUCCESS.getTemplate(), variables);
        sendEmail(order.getCustomer().getEmail(), ORDER_SUCCESS.getSubject(), htmlContent);
        notificationsService.create(NotificationType.ORDER_SUCCESS, EMAIL);
    }

    @Override
    @Async
    public void sendEmailUpdateVerificationEmail(User user, String newEmail, String link) {
        Map<String, Object> variables = createEmailUpdateVerificationEmailVariables(user, newEmail, link);
        String htmlContent = processTemplate(EMAIL_UPDATE_VERIFICATION.getTemplate(), variables);
        sendEmail(newEmail, EMAIL_UPDATE_VERIFICATION.getSubject(), htmlContent);
        notificationsService.create(NotificationType.EMAIL_UPDATE_VERIFICATION, EMAIL);
    }

    private Map<String, Object> createEmailVerificationEmailVariables(User user, String link) {
        Map<String, Object> variables = new HashMap<>();
        String formattedRegistrationDate = user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));

        variables.put("customerName", user.getUserName());
        variables.put("customerEmail", user.getEmail());
        variables.put("senderEmail", senderEmail);
        variables.put("registrationDate", formattedRegistrationDate);
        variables.put("verificationUrl", link);

        return variables;
    }

    private Map<String, Object> createPasswordResetEmailVariables(User user, String link) {
        Map<String, Object> variables = new HashMap<>();
        String formattedRequestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));

        variables.put("customerName", user.getUserName());
        variables.put("customerEmail", user.getEmail());
        variables.put("senderEmail", senderEmail);
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
        variables.put("senderEmail", senderEmail);
        variables.put("paymentMethod", paymentData.getPaymentMethod());
        variables.put("paymentTotal", paymentData.getTotal());
        variables.put("orderReference", order.getOrderReference());
        variables.put("createdAt", formattedOrderDate);
        variables.put("ticketList", ticketNumbers);
        variables.put("ticketCount", ticketNumbers.size());

        return variables;
    }

    private Map<String, Object> createEmailUpdateVerificationEmailVariables(User user, String newEmail, String link) {
        Map<String, Object> variables = new HashMap<>();
        String formattedRequestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));

        variables.put("customerName", user.getUserName());
        variables.put("customerEmail", user.getEmail());
        variables.put("newEmail", newEmail);
        variables.put("senderEmail", senderEmail);
        variables.put("requestDate", formattedRequestDate);
        variables.put("updateUrl", link);

        return variables;
    }

    private String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            return templateEngine.process(templateName, context);
        } catch (Exception ex) {
            log.error("Error processing email template {}: {}", templateName, ex.getMessage());
            throw new CustomMailException("Error processing email template: " + ex.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MailRequest emailRequest = MailRequest.builder()
                    .sender(senderEmail)
                    .to(List.of(to))
                    .subject(subject)
                    .htmlBody(htmlContent)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Smtp2go-Api-Key", apiKey);
            headers.set("accept", "application/json");

            HttpEntity<MailRequest> requestEntity = new HttpEntity<>(emailRequest, headers);

            // Construct the complete endpoint URL
            String emailEndpoint = apiUrl + "/email/send";

            ResponseEntity<MailResponse> response = restTemplate.exchange(
                    emailEndpoint,
                    HttpMethod.POST,
                    requestEntity,
                    MailResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MailResponse responseBody = response.getBody();
                if (responseBody.getData() != null && responseBody.getData().getSucceeded() != null && responseBody.getData().getSucceeded() > 0) {
                    log.info("Email sent successfully to {} with email ID: {}", to, responseBody.getData().getEmailId());
                } else {
                    String errorMessage = responseBody.getData() != null ? responseBody.getData().getError() : "Unknown error";
                    throw new CustomMailException("Failed to send email: " + errorMessage);
                }
            } else {
                throw new CustomMailException("Failed to send email: HTTP " + response.getStatusCode());
            }

        } catch (RestClientException ex) {
            log.error("Error sending email to {}: {}", to, ex.getMessage());
            throw new CustomMailException("Error occurred while sending email: " + ex.getMessage());
        }
    }
}
