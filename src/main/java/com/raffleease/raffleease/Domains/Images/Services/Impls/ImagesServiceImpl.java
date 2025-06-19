package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Model.ImageStatus;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ImagesServiceImpl implements ImagesService {
    private final FileStorageService fileStorageService;
    private final ImagesRepository repository;

    @Value("${spring.application.hosts.server}")
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
    public Resource getFile(Long id) {
        Image image = findById(id);
        
        // Prevent access to images marked for deletion
        if (image.getStatus() == ImageStatus.MARKED_FOR_DELETION) {
            throw new NotFoundException("Image not found for id <" + id + ">");
        }
        
        return fileStorageService.load(image.getFilePath());
    }
}