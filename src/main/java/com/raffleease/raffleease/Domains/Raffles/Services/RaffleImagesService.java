package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;
import com.raffleease.raffleease.Domains.Raffles.Repository.ImagesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RaffleImagesService {
    private final ImagesRepository imagesRepository;

    public List<RaffleImage> saveImages(List<RaffleImage> images) {
        try {
            return imagesRepository.saveAll(images.stream().toList());
        } catch (Exception exp) {
            throw new DatabaseException("Failed to access database when saving images: " + exp.getMessage());
        }
    }
}
