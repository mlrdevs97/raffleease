package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Tickets.DTO.SearchRequest;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketDTO;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Set;

public interface ITicketsQueryService {
    List<Ticket> findAllById(Set<Long> ticketsIds);
    List<TicketDTO> findByTicketNumber(SearchRequest request);
}
