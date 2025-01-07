package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageFile;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.IFIleStorage;
import com.raffleease.raffleease.Domains.Images.Services.IImagesService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements IImagesService {
    private final IFIleStorage fileStorage;
    private final ImagesRepository repository;

    @Override
    public List<Image> create(Raffle raffle, List<MultipartFile> files) {
        List<Image> images = files.stream().map(file -> {
            String storedPath = fileStorage.save(file);
            return Image.builder()
                    .filePath(storedPath)
                    .originalName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .raffle(raffle)
                    .build();
        }).toList();
        return saveAll(images);
    }

    @Override
    public ImageFile get(Image image) {
        byte[] data = fileStorage.load(image.getFilePath());
        return ImageFile.builder()
                .data(data)
                .contentType(image.getContentType())
                .build();
    }

    @Override
    public List<ImageFile> getAll(List<Image> images) {
        return images.stream().map(image -> {
            byte[] data = fileStorage.load(image.getFilePath());
            return ImageFile.builder()
                    .data(data)
                    .contentType(image.getContentType())
                    .build();
        }).toList();
    }

    @Override
    public void delete(Long id) {
        Image image = findById(id);
        fileStorage.delete(image.getFilePath());
        try {
            repository.delete(image);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting image: " + ex.getMessage());
        }
    }

    @Override
    public void deleteAll(List<Image> images) {
        if (images.isEmpty()) return;
        images.forEach(image -> fileStorage.delete(image.getFilePath()));
        try {
            repository.deleteAll(images);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting images: " + ex.getMessage());
        }
    }

    private Image findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Image not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while retrieving image with ID <" + id + ">: " + ex.getMessage());
        }
    }

    private List<Image> saveAll(List<Image> entities) {
        try {
            return repository.saveAll(entities);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving images: " + ex.getMessage());
        }
    }
}
