package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.util.List;

public interface ImagesAssociateService {
    List<Image> associateImagesToRaffleOnCreate(Raffle raffle, List<ImageDTO> imageDTOs);
    List<Image> associateImagesToRaffleOnEdit(Raffle raffle, List<ImageDTO> imageDTOs);
}
