package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.*;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface ImagesService {
    ImageResponse create(HttpServletRequest request, ImageUpload uploadRequest);
    List<Image> associateImagesToRaffleOnCreate(Raffle raffle, List<ImageDTO> imageDTOs);
    List<Image> associateImagesToRaffleOnEdit(Raffle raffle, List<ImageDTO> imageDTOs);
    List<Image> saveAll(List<Image> images);
    Image findById(Long id);
    List<Image> findAllById(List<Long> ids);
    ImageFile getFile(Long id);
}