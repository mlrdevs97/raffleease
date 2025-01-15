package com.raffleease.raffleease.Domains.Raffles.Services;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.PublicRaffleDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRaffleCreateService {
    PublicRaffleDTO createRaffle(String authHeader, RaffleCreate request, List<MultipartFile> images);
}
