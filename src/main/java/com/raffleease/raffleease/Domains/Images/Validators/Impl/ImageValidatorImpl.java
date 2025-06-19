package com.raffleease.raffleease.Domains.Images.Validators.Impl;

import com.raffleease.raffleease.Common.Constants.Constants;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Model.ImageStatus;
import com.raffleease.raffleease.Domains.Images.Validators.ImageValidator;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Domains.Users.Model.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.raffleease.raffleease.Common.Constants.Constants.MAX_IMAGES;
import static com.raffleease.raffleease.Domains.Images.Model.ImageStatus.ACTIVE;
import static com.raffleease.raffleease.Domains.Images.Model.ImageStatus.PENDING;

@Component
public class ImageValidatorImpl implements ImageValidator {
    @Override
    public <T> void validateNoDuplicates(List<T> list, String message) {
        if (hasDuplicates(list)) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void validatePendingImagesBelongToUser(User user, List<Image> images) {
        if (images.stream().anyMatch(image -> !image.getUser().equals(user))) {
            throw new AuthorizationException("You are not authorized to use the specified pending image(s)");
        }
    }

    @Override
    public void validateTotalImagesNumber(long uploadingImagesCount, long currentImagesCount) {
        if (currentImagesCount + uploadingImagesCount > MAX_IMAGES) {
            throw new BusinessException("You cannot upload more than 10 images in total");
        }
    }

    @Override
    public void validatePendingImagesNotAssociatedWithRaffle(List<Image> images) {
        boolean hasInvalidImages = images.stream()
                .anyMatch(image -> image.getStatus().equals(PENDING) && image.getRaffle() != null);

        if (hasInvalidImages) {
            throw new BusinessException("Pending images cannot be associated with a raffle.");
        }
    }

    @Override
    public void validateActiveImagesBelongToRaffle(Raffle raffle, List<Image> images) {
        boolean hasInvalidImages = images.stream()
                .filter(image -> !image.getStatus().equals(PENDING))
                .anyMatch(image -> !image.getStatus().equals(ACTIVE) || !raffle.equals(image.getRaffle()));

        if (hasInvalidImages) {
            throw new BusinessException("Non-pending images must have ACTIVE status and belong to the specified raffle.");
        }
    }

    @Override
    public void validateAllArePending(List<Image> images) {
        boolean anyLinked = images.stream().anyMatch(img -> !img.getStatus().equals(PENDING));
        if (anyLinked) {
            throw new BusinessException("Only pending images can be associated with a raffle.");
        }
    }

    @Override
    public void validateAtLeastOneImage(List<Image> existingImages) {
        if (existingImages.isEmpty()) {
            throw new BusinessException("A raffle must have at least one image.");
        }
    }

    @Override
    public void validateImagesBelongToAssociation(Association association, List<Image> images) {
        if (images.stream().anyMatch(image -> !image.getAssociation().equals(association))) {
            throw new AuthorizationException("You are not authorized to use the specified image(s)");
        }
    }

    private <T> boolean hasDuplicates(List<T> list) {
        Set<T> set = new HashSet<>();
        return list.stream().anyMatch(element -> !set.add(element));
    }

    private boolean areConsecutive(List<Integer> numbers) {
        List<Integer> sorted = numbers.stream().sorted().toList();
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) != i + 1) return false;
        }
        return true;
    }
}