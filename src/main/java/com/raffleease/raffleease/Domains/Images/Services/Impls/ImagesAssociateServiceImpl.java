package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.ImagesAssociateService;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Domains.Images.Validators.ImageValidator;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ImagesAssociateServiceImpl implements ImagesAssociateService {
    private final FileStorageService fileStorageService;
    private final ImagesRepository repository;
    private final ImageValidator imageValidator;

    @Value("${spring.application.hosts.server}")
    private String host;

    @Override
    public List<Image> associateImagesToRaffleOnCreate(Raffle raffle, List<ImageDTO> imageDTOs) {
        // Step 1: Perform validations
        List<Long> imageIds = imageDTOs.stream().map(ImageDTO::id).toList();
        List<Integer> imageOrders = imageDTOs.stream().map(ImageDTO::imageOrder).toList();

        imageValidator.validateNoDuplicates(imageIds, "Duplicate image IDs found in request");
        imageValidator.validateNoDuplicates(imageOrders, "Duplicate image orders detected");

        List<Image> images = repository.findAllById(imageIds);
        imageValidator.validateAllImagesExist(imageIds, images);
        imageValidator.validateImagesBelongToAssociation(raffle.getAssociation(), images);
        imageValidator.validateAllArePending(images);

        // Step 2: Process and link all new images in the request with correct order
        Map<Long, Integer> orderMap = imageDTOs.stream().collect(Collectors.toMap(ImageDTO::id, ImageDTO::imageOrder));

        for (Image image : images) {
            Path finalPath = fileStorageService.moveFileToRaffle(
                    String.valueOf(raffle.getAssociation().getId()),
                    String.valueOf(raffle.getId()),
                    String.valueOf(image.getId()),
                    image.getFilePath()
            );
            image.setImageOrder(orderMap.get(image.getId()));
            image.setFilePath(finalPath.toString());
            image.setUrl(host + "/api/v1/associations/" + raffle.getAssociation().getId() + "/raffles/" + raffle.getId() + "/images/" + image.getId());
            image.setRaffle(raffle);
        }
        return images;
    }

    @Override
    public List<Image> associateImagesToRaffleOnEdit(Raffle raffle, List<ImageDTO> imageDTOs) {
        // Step 1: Perform validations
        List<Long> imageIds = imageDTOs.stream().map(ImageDTO::id).toList();
        List<Integer> imageOrders = imageDTOs.stream().map(ImageDTO::imageOrder).toList();

        imageValidator.validateNoDuplicates(imageIds, "Duplicate image IDs found in request");
        imageValidator.validateNoDuplicates(imageOrders, "Duplicate image orders detected");

        List<Image> images = repository.findAllById(imageIds);
        imageValidator.validateAllImagesExist(imageIds, images);
        imageValidator.validateImagesBelongToAssociation(raffle.getAssociation(), images);
        imageValidator.validateImagesArePendingOrBelongToRaffle(raffle, images);

        // Step 2: Unlink any images that were previously associated to this raffle but are not in the new list
        raffle.getImages().removeIf(image -> !imageIds.contains(image.getId()));

        // Step 3: Process and link all new images in the request with correct order
        Map<Long, Integer> imageOrderMap = imageDTOs.stream().collect(Collectors.toMap(ImageDTO::id, ImageDTO::imageOrder));

        for (Image image : images) {
            // Only move files if the image was still pending
            if (image.getRaffle() == null) {
                Path finalPath = fileStorageService.moveFileToRaffle(
                        String.valueOf(raffle.getAssociation().getId()),
                        String.valueOf(raffle.getId()),
                        String.valueOf(image.getId()),
                        image.getFilePath()
                );
                image.setFilePath(finalPath.toString());
                image.setUrl(host + "/api/v1/associations/" + raffle.getAssociation().getId() + "/raffles/" + raffle.getId() + "/images/" + image.getId());
                image.setRaffle(raffle);
            }
            image.setImageOrder(imageOrderMap.get(image.getId()));
        }
        return images;
    }
}