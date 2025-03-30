package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.DeleteImagesService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DeleteImagesServiceImpl implements DeleteImagesService {
    private final AssociationsService associationsService;
    private final ImagesService imagesService;
    private final ImagesRepository repository;

    @Override
    @Transactional
    public void deleteImage(HttpServletRequest request, Long id) {
        Image image = imagesService.findById(id);
        if (image.getRaffle() != null) {
            throw new BusinessException("You cannot delete an image already associated with a raffle");
        }

        Association association = associationsService.findFromRequest(request);
        if (!image.getAssociation().equals(association)) {
            throw new AuthorizationException("You are not authorized to delete this image");
        }

        delete(image);
        updateImagesOrder(association, image.getImageOrder());
    }

    @Override
    public void deleteAll(List<Image> images) {
        try {
            repository.deleteAll(images);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting images: " + ex.getMessage());
        }
    }

    private void updateImagesOrder(Association association, int deletedImageOrder) {
        List<Image> images = repository.findAllByRaffleIsNullAndAssociationAndImageOrderGreaterThan(association, deletedImageOrder);
        images.forEach(image -> image.setImageOrder(image.getImageOrder() - 1));
        imagesService.saveAll(images);
    }

    private void delete(Image image) {
        try {
            repository.delete(image);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while deleting image: " + ex.getMessage());
        }
    }
}
