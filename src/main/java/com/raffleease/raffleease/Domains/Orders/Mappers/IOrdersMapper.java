package com.raffleease.raffleease.Domains.Orders.Mappers;

import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;
import com.raffleease.raffleease.Domains.Orders.Model.Order;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IOrdersMapper {
    OrderDTO fromOrder(Order order, List<Ticket> purchasedTickets);
}
