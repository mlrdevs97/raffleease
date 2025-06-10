package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;
import com.raffleease.raffleease.Domains.Images.Mappers.ImagesMapper;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesCreateService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Domains.Images.Validators.ImageValidator;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ImagesCreateServiceImpl implements ImagesCreateService {
    private final ImagesService imagesService;
    private final FileStorageService fileStorageService;
    private final ImagesRepository repository;
    private final ImagesMapper mapper;
    private final AssociationsService associationsService;
    private final RafflesPersistenceService rafflesPersistenceService;
    private final ImageValidator imageValidator;

    @Value("${spring.application.hosts.server}")
    private String host;

    @Override
    @Transactional
    public ImageResponse create(Long associationId, ImageUpload uploadRequest) {
        String baseURL = host + "/v1/associations/" + associationId + "/images/";
        return processImagesCreation(associationId, uploadRequest, 0, baseURL, null);
    }

    @Override
    @Transactional
    public ImageResponse create(Long associationId, Long raffleId, ImageUpload uploadRequest) {
        Raffle raffle = rafflesPersistenceService.findById(raffleId);
        int currentImagesCount = raffle.getImages().size();
        String baseURL = host + "/v1/associations/" + associationId + "/raffles/" + raffleId + "/images/";
        return processImagesCreation(associationId, uploadRequest, currentImagesCount, baseURL, raffle);
    }

    private ImageResponse processImagesCreation(Long associationId, ImageUpload uploadRequest, int currentImagesCount, String baseURL, Raffle raffle) {
        Association association = associationsService.findById(associationId);
        List<MultipartFile> files = uploadRequest.files();
        int pendingImagesCount = repository.countPendingImagesByAssociation(association);
        int totalImagesCount = pendingImagesCount + currentImagesCount;
        imageValidator.validateTotalImagesNumber(files.size(), totalImagesCount);
        List<Image> images = mapAndSaveFiles(association, files, raffle);

        for(int i = 0; i < images.size(); i++) {
            MultipartFile file = files.get(i);
            Image image = images.get(i);

            String storedPath = fileStorageService.save(file, String.valueOf(association.getId()), String.valueOf(image.getId()));
            image.setFilePath(storedPath);
            image.setUrl(baseURL + image.getId());
            image.setImageOrder(totalImagesCount + i + 1);
        }
        List<ImageDTO> mappedImages = mapper.fromImagesList(imagesService.saveAll(images));
        return new ImageResponse(mappedImages);
    }

    private List<Image> mapAndSaveFiles(Association association, List<MultipartFile> files, Raffle raffle) {
        return imagesService.saveAll(files.stream().map(file -> Image.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .association(association)
                .raffle(raffle)
                .build()).toList());
    }
}