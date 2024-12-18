package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.Model.RaffleImage;

import java.util.List;

public interface IRaffleImagesService {
    List<RaffleImage> saveImages(List<RaffleImage> images);
}
