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
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements ImagesService {
    private final FileStorageService fileStorageService;
    private final ImagesRepository repository;
    private final ImagesMapper mapper;
    private final AssociationsService associationsService;
    private final RafflesPersistenceService rafflesPersistenceService;
    private final ImageValidator imageValidator;

    @Value("${spring.application.host.server}")
    private String host;

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
}