package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ImagesDeleteServiceImpl implements ImagesDeleteService {
    private final AssociationsService associationsService;
    private final ImagesService imagesService;
    private final RafflesPersistenceService rafflesPersistenceService;
    private final ImagesRepository repository;

    @Override
    @Transactional
    public void deleteImage(Long associationId, Long id) {
        Image image = imagesService.findById(id);

        Association association = associationsService.findById(associationId);
        if (!image.getAssociation().equals(association)) {
            throw new AuthorizationException("You are not authorized to delete this image");
        }

        if (Objects.nonNull(image.getRaffle())) {
            Raffle raffle = image.getRaffle();
            List<Image> raffleImages = image.getRaffle().getImages();
            raffleImages.remove(image);
            image.setRaffle(null);
            rafflesPersistenceService.save(raffle);
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
