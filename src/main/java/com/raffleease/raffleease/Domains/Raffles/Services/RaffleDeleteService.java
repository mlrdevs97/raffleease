package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RaffleDeleteService {
    private final TicketsDeleteProducer ticketsDeleteProducer;
    private final RafflesQueryService queryService;
    private final S3Service s3Service;
    private final IRafflesRepository repository;
    private final RafflesStatusService rafflesStatusService;

    @Transactional
    public void delete(Long id) {
        Raffle raffle = queryService.findById(id);
        rafflesStatusService.delete(id);
        List<String> images = raffle.getImages().stream().map(RaffleImage::getKey).collect(Collectors.toList());
        deleteRegistry(id);
        ticketsDeleteProducer.deleteTickets(
                TicketsDelete.builder()
                        .raffleId(id)
                        .build()
        );
        CompletableFuture.runAsync(() -> s3Service.delete(images));
    }

    public void deleteRegistry(Long id) {
        try {
            repository.deleteById(id);
        } catch (DataAccessException ex) {
            throw new DatabaseException(" " + ex.getMessage());
        }
    }

}
