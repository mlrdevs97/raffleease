package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.ImagesAssociateService;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import com.raffleease.raffleease.Domains.Images.Validators.ImageValidator;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
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
    private final ImagesDeleteService deleteService;
    private final FileStorageService fileStorageService;
    private final ImagesRepository repository;
    private final ImageValidator imageValidator;

    @Value("${spring.application.hosts.server}")
    private String host;

    @Override
    public List<Image> associateImagesToRaffleOnCreate(Raffle raffle, List<ImageDTO> imageDTOs) {
        // 1: Perform validations
        List<Long> imageIds = imageDTOs.stream().map(ImageDTO::id).toList();
        List<Integer> imageOrders = imageDTOs.stream().map(ImageDTO::imageOrder).toList();
        imageValidator.validateNoDuplicates(imageIds, "Duplicate image IDs found in request");
        imageValidator.validateNoDuplicates(imageOrders, "Duplicate image orders detected");

        // 2: Find existing images and separate valid from missing
        List<Image> existingImages = repository.findAllById(imageIds);
        List<Long> existingImageIds = existingImages.stream().map(Image::getId).toList();
        List<Long> missingImageIds = imageIds.stream()
                .filter(id -> !existingImageIds.contains(id))
                .toList();

        // 3: Clean up missing pending images
        if (!missingImageIds.isEmpty()) {
            removePendingImages(missingImageIds);
        }

        // 4: Validate existing images
        imageValidator.validateAtLeastOneImage(existingImages);
        imageValidator.validateImagesBelongToAssociation(raffle.getAssociation(), existingImages);
        imageValidator.validateAllArePending(existingImages);

        // 5: Filter DTOs to only include existing images
        List<ImageDTO> validImageDTOs = imageDTOs.stream()
                .filter(dto -> existingImageIds.contains(dto.id()))
                .toList();

        // 6: Process and link all existing images in the request with correct order
        Map<Long, Integer> orderMap = validImageDTOs.stream()
                .collect(Collectors.toMap(ImageDTO::id, ImageDTO::imageOrder));

        for (Image image : existingImages) {
            // For new raffles, we'll set the URL and path after the raffle is saved
            image.setImageOrder(orderMap.get(image.getId()));
            image.setRaffle(raffle);
        }
        
        return existingImages;
    }

    @Override
    public List<Image> associateImagesToRaffleOnEdit(Raffle raffle, List<ImageDTO> imageDTOs) {
        // 1: Perform validations
        List<Long> imageIds = imageDTOs.stream().map(ImageDTO::id).toList();
        List<Integer> imageOrders = imageDTOs.stream().map(ImageDTO::imageOrder).toList();
        imageValidator.validateNoDuplicates(imageIds, "Duplicate image IDs found in request");
        imageValidator.validateNoDuplicates(imageOrders, "Duplicate image orders detected");

        // 2: Find existing images and separate valid from missing
        List<Image> existingImages = repository.findAllById(imageIds);
        List<Long> existingImageIds = existingImages.stream().map(Image::getId).toList();
        List<Long> missingImageIds = imageIds.stream()
                .filter(id -> !existingImageIds.contains(id))
                .toList();

        // 3: Clean up missing pending images
        if (!missingImageIds.isEmpty()) {
            removePendingImages(missingImageIds);
        }

        // 4: Validate existing images (do this before making any changes)
        imageValidator.validateAtLeastOneImage(existingImages);
        imageValidator.validateImagesBelongToAssociation(raffle.getAssociation(), existingImages);
        imageValidator.validateImagesArePendingOrBelongToRaffle(raffle, existingImages);

        // 5: Remove any existing raffle images that are no longer in the request
        List<Long> currentRaffleImageIds = raffle.getImages().stream()
                .map(Image::getId)
                .toList();
        
        List<Long> imagesToRemoveFromRaffle = currentRaffleImageIds.stream()
                .filter(id -> !imageIds.contains(id))
                .toList();
        
        if (!imagesToRemoveFromRaffle.isEmpty()) {
            raffle.getImages().removeIf(image -> imagesToRemoveFromRaffle.contains(image.getId()));
        }

        // 6: Filter DTOs to only include existing images
        List<ImageDTO> validImageDTOs = imageDTOs.stream()
                .filter(dto -> existingImageIds.contains(dto.id()))
                .toList();

        // 7: Process and link all images in the request with correct order
        Map<Long, Integer> imageOrderMap = validImageDTOs.stream()
                .filter(dto -> existingImageIds.contains(dto.id()))
                .collect(Collectors.toMap(ImageDTO::id, ImageDTO::imageOrder));

        for (Image image : existingImages) {
            if (image.getRaffle() == null) {
                Path finalPath = fileStorageService.moveFileToRaffle(
                        String.valueOf(raffle.getAssociation().getId()),
                        String.valueOf(raffle.getId()),
                        String.valueOf(image.getId()),
                        image.getFilePath()
                );
                image.setFilePath(finalPath.toString());
                image.setUrl(host + "/v1/associations/" + raffle.getAssociation().getId() + "/raffles/" + raffle.getId() + "/images/" + image.getId());
                image.setRaffle(raffle);
            }
            
            Integer newOrder = imageOrderMap.get(image.getId());
            if (newOrder != null) {
                image.setImageOrder(newOrder);
            }
        }
        
        return existingImages;
    }

    private void removePendingImages(List<Long> missingImageIds) {
        List<Image> pendingImages = repository.findAllById(missingImageIds);
        deleteService.deleteAll(pendingImages);
    }

    public void finalizeImagePathsAndUrls(Raffle raffle, List<Image> images) {
        for (Image image : images) {
            Path finalPath = fileStorageService.moveFileToRaffle(
                    String.valueOf(raffle.getAssociation().getId()),
                    String.valueOf(raffle.getId()),
                    String.valueOf(image.getId()),
                    image.getFilePath()
            );
            image.setFilePath(finalPath.toString());
            image.setUrl(host + "/v1/associations/" + raffle.getAssociation().getId() + "/raffles/" + raffle.getId() + "/images/" + image.getId());
        }
    }
}