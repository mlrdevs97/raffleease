package com.raffleease.raffleease.Domains.Raffles.Services;

public interface IAvailabilityService {
    void modifyTicketsAvailability(Long raffleId, Long quantity, byte operationType);
}
