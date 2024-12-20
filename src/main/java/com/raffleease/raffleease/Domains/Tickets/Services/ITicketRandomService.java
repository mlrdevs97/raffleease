package com.raffleease.raffleease.Domains.Tickets.Services;

import com.raffleease.raffleease.Domains.Tickets.DTO.GenerateRandom;
import com.raffleease.raffleease.Domains.Tickets.DTO.ReservationResponse;

public interface ITicketRandomService {
    ReservationResponse generateRandom(GenerateRandom request);
}
