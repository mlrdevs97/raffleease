package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ImagesDeleteServiceImpl implements ImagesDeleteService {
    private final ImagesRepository repository;

    @Override
    public void deleteAll(List<Image> images) {
        try {
            repository.deleteAll(images);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting images: " + ex.getMessage());
        }
    }

    @Override
    public void delete(Image image) {
        try {
            repository.delete(image);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting image: " + ex.getMessage());
        }
    }
}
