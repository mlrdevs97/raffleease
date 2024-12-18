package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;
import com.raffleease.raffleease.Domains.Raffles.Repository.IRafflesRepository;
import com.raffleease.raffleease.Domains.Raffles.Services.IRaffleDeleteService;
import com.raffleease.raffleease.Domains.Raffles.Services.IRafflesQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RaffleDeleteServiceImpl implements IRaffleDeleteService {
    private final ITicketsDeleteService ticketsDeleteService;
    private final IRafflesQueryService queryService;
    private final S3Service s3Service;
    private final IRafflesRepository repository;
    private final RafflesStatusServiceImpl rafflesStatusServiceImpl;

    @Transactional
    public void delete(Long id) {
        Raffle raffle = queryService.findById(id);
        rafflesStatusServiceImpl.delete(id);
        List<String> images = raffle.getImages().stream().map(RaffleImage::getKey).toList();
        deleteRegistry(id);
        ticketsDeleteService.deleteTickets(id);
        // CompletableFuture.runAsync(() -> s3Service.delete(images));
    }

    private void deleteRegistry(Long id) {
        try {
            repository.deleteById(id);
        } catch (DataAccessException ex) {
            throw new DatabaseException(" " + ex.getMessage());
        }
    }
}
