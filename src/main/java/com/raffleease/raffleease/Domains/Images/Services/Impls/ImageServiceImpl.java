package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.DTOs.*;
import com.raffleease.raffleease.Domains.Images.Mappers.ImagesMapper;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Domains.Images.Validators.ImageValidator;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Exceptions.CustomExceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements ImagesService {
    private final FileStorageService fileStorageService;
    private final ImagesRepository repository;
    private final ImagesMapper mapper;
    private final AssociationsService associationsService;
    private final ImageValidator imageValidator;

    @Value("${spring.application.host.server}")
    private String host;

    @Override
    @Transactional
    public ImageResponse create(HttpServletRequest request, ImageUpload uploadRequest) {
        Association association = associationsService.findFromRequest(request);
        List<MultipartFile> files = uploadRequest.files();
        long existingImagesCount = repository.countPendingImagesByAssociation(association);
        imageValidator.validateTotalImagesNumber(files.size(), existingImagesCount);
        List<Image> images = mapAndSaveFiles(association, files);

        for(int i = 0; i < images.size(); i++) {
            MultipartFile file = files.get(i);
            Image image = images.get(i);

            try {
                String storedPath = fileStorageService.save(file, String.valueOf(association.getId()), String.valueOf(image.getId()));
                image.setFilePath(storedPath);
                image.setUrl(host + "/api/v1/images/" + image.getId());
                image.setImageOrder(i + 1);
            } catch (FileStorageException ex) {
                log.error("Failed to process file: " + file.getOriginalFilename(), ex);
            }
        }
        return new ImageResponse(mapper.fromImagesList(saveAll(images)));
    }

    @Override
    @Transactional
    public List<Image> associateImagesToRaffleOnCreate(Raffle raffle, List<ImageDTO> imageDTOs) {
        List<Long> imageIds = imageDTOs.stream().map(ImageDTO::id).toList();
        imageValidator.validateNoDuplicates(imageIds, "Duplicate image IDs found in request");

        List<Integer> imageOrders = imageDTOs.stream()
                .map(ImageDTO::imageOrder)
                .toList();
        imageValidator.validateNoDuplicates(imageOrders, "Duplicate image orders detected");
        imageValidator.validateConsecutiveOrders(imageOrders);

        List<Image> images = repository.findAllById(imageIds);
        imageValidator.validateAllImagesExist(imageIds, images);
        imageValidator.validateImagesBelongToAssociation(raffle.getAssociation(), images);

        for (Image image : images) {
            Path finalPath = fileStorageService.moveFileToRaffle(
                    String.valueOf(raffle.getAssociation().getId()),
                    String.valueOf(raffle.getId()),
                    String.valueOf(image.getId()),
                    image.getFilePath()
            );
            image.setFilePath(finalPath.toString());
            image.setUrl(host + "/api/v1/raffles/" + raffle.getId() + "/images/" + image.getId());
            image.setRaffle(raffle);
        }
        return images;
    }

    @Override
    @Transactional
    public List<Image> associateImagesToRaffleOnEdit(Raffle raffle, List<ImageDTO> imageDTOs) {
        // Extract and validate data from DTOs
        List<Long> imageIds = imageDTOs.stream().map(ImageDTO::id).toList();
        imageValidator.validateNoDuplicates(imageIds, "Duplicate image IDs found in request");

        List<Integer> imageOrders = imageDTOs.stream().map(ImageDTO::imageOrder).toList();
        imageValidator.validateNoDuplicates(imageOrders, "Duplicate image orders detected");
        imageValidator.validateConsecutiveOrders(imageOrders);

        List<Image> images = repository.findAllById(imageIds);
        imageValidator.validateAllImagesExist(imageIds, images);
        imageValidator.validateImagesBelongToAssociation(raffle.getAssociation(), images);

        // Step 2: Ensure each image is either pending or already belongs to the current raffle
        imageValidator.validateImagesArePendingOrBelongToRaffle(raffle, images);

        // Step 3: Unlink any images that were previously associated to this raffle but are not in the new list
        List<Image> currentImages = repository.findAllByRaffle(raffle);
        for (Image image : currentImages) {
            if (!imageIds.contains(image.getId())) {
                image.setRaffle(null);
                image.setFilePath(null);
                image.setUrl(null);
            }
        }

        // Step 4: Process and link all images in the new request
        Map<Long, Integer> imageOrderMap = imageDTOs.stream()
                .collect(Collectors.toMap(ImageDTO::id, ImageDTO::imageOrder));

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
                image.setUrl(host + "/api/v1/raffles/" + raffle.getId() + "/images/" + image.getId());
            }
            image.setRaffle(raffle);
            image.setImageOrder(imageOrderMap.get(image.getId()));
        }

        return images;
    }

    @Override
    public List<Image> saveAll(List<Image> images) {
        try {
            return repository.saveAll(images);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving images: " + ex.getMessage());
        }
    }

    @Override
    public Image findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Image not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while retrieving image with ID <" + id + ">: " + ex.getMessage());
        }
    }

    @Override
    public List<Image> findAllById(List<Long> ids) {
        try {
            List<Image> images = repository.findAllById(ids);
            if(images.isEmpty()) throw new NotFoundException("No images were found for provided ids");
            return images;
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while retrieving images by ID: " + ex.getMessage());
        }
    }

    @Override
    public ImageFile getFile(Long id) {
        Image image = findById(id);
        byte[] data = fileStorageService.load(image.getFilePath());
        return ImageFile.builder()
                .data(data)
                .contentType(image.getContentType())
                .build();
    }

    private List<Image> mapAndSaveFiles(Association association, List<MultipartFile> files) {
        return saveAll(files.stream().map(file -> Image.builder()
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .association(association)
                        .createdAt(LocalDateTime.now())
                        .build()).toList());
    }
}