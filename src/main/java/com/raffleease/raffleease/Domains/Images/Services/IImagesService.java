package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageFile;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImagesService {
    List<Image> create(Raffle raffle, List<MultipartFile> files);

    ImageFile get(Image image);

    List<ImageFile> getAll(List<Image> images);

    void delete(Long id);

    void deleteAll(List<Image> images);
}

