package com.raffleease.raffleease.Domains.Raffles.Services.Impl;

import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;
import com.raffleease.raffleease.Domains.Raffles.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Raffles.Services.IRaffleImagesService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RaffleImagesServiceImpl implements IRaffleImagesService {
    private final ImagesRepository imagesRepository;

    public List<RaffleImage> saveImages(List<RaffleImage> images) {
        try {
            return imagesRepository.saveAll(images.stream().toList());
        } catch (Exception ex) {
            throw new DatabaseException("Database error occurred while saving images: " + ex.getMessage());
        }
    }
}
