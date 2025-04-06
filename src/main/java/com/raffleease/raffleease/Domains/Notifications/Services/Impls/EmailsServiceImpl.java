package com.raffleease.raffleease.Domains.Notifications.Services.Impls;

import com.raffleease.raffleease.Domains.Customers.Model.Customer;
import com.raffleease.raffleease.Domains.Notifications.Services.IEmailsService;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Payments.Model.Payment;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.raffleease.raffleease.Domains.Notifications.Model.EmailTemplates.ORDER_SUCCESS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;

@RequiredArgsConstructor
@Service
public class EmailsServiceImpl implements IEmailsService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${MAIL_USERNAME}")
    private String userName;

    @Async
    public void sendOrderSuccessNotification(Order order) throws MessagingException {
        Map<String, Object> variables = createOrderSuccessNotificationVariables(order);
        sendEmail(order.getCustomer().getEmail(),
                ORDER_SUCCESS.getSubject(),
                ORDER_SUCCESS.getTemplate(),
                variables
        );
    }

    private Map<String, Object> createOrderSuccessNotificationVariables(Order order) {
        Map<String, Object> variables = new HashMap<>();
        Payment paymentData = order.getPayment();
        Customer customer = order.getCustomer();
        String formattedOrderDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));

        List<String> ticketNumbers = order.getCart().getTickets().stream()
                .map(Ticket::getTicketNumber)
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
        variables.put("orderDate", formattedOrderDate);
        variables.put("ticketList", ticketNumbers);
        variables.put("ticketCount", ticketNumbers.size());

        return variables;
    }


    private void sendEmail(String to,
                           String subject,
                           String templateName,
                           Map<String, Object> variables
    ) throws MessagingException {
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
    }
}
