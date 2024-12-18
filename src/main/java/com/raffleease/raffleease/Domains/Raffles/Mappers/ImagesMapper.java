package com.raffleease.raffleease.Domains.Raffles.Mappers;

import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;

import java.util.List;
import java.util.stream.Collectors;

public class ImagesMapper {
    public List<String> fromImages(List<RaffleImage> images) {
        return images.stream()
                .map(RaffleImage::getKey)
                .collect(Collectors.toList());
    }

    public List<RaffleImage> toImages(List<String> keys, Raffle raffle) {
        return keys.stream()
                .map(key -> RaffleImage.builder()
                        .raffle(raffle)
                        .key(key)
                        .build())
                .collect(Collectors.toList());
    }
}
