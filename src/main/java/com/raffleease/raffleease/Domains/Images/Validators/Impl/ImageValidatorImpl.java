package com.raffleease.raffleease.Domains.Images.Validators.Impl;

import com.raffleease.raffleease.Common.Constants.Constants;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Validators.ImageValidator;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ImageValidatorImpl implements ImageValidator {
    @Override
    public <T> void validateNoDuplicates(List<T> list, String message) {
        if (hasDuplicates(list)) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void validateImagesBelongToAssociation(Association association, List<Image> images) {
        if (images.stream().anyMatch(image -> !image.getAssociation().equals(association))) {
            throw new AuthorizationException("You are not authorized to use the specified image(s)");
        }
    }

    @Override
    public void validateConsecutiveOrders(List<Integer> orders) {
        if (!areConsecutive(orders)) {
            throw new IllegalArgumentException("Image orders must be consecutive starting from 1");
        }
    }

    @Override
    public void validateTotalImagesNumber(long uploadingImagesCount, long currentImagesCount) {
        if (currentImagesCount + uploadingImagesCount > Constants.MAX_IMAGES) {
            throw new BusinessException("You cannot upload more than 10 images in total");
        }
    }

    @Override
    public void validateAllImagesExist(List<Long> requestedIds, List<Image> foundImages) {
        if (requestedIds.size() > foundImages.size()) {
            throw new NotFoundException("One or more images were not found");
        }
    }

    @Override
    public void validateImagesArePendingOrBelongToRaffle(Raffle raffle, List<Image> images) {
        List<Long> invalidImageIds = images.stream()
                .filter(image -> image.getRaffle() != null && !image.getRaffle().equals(raffle))
                .map(Image::getId)
                .toList();

        if (!invalidImageIds.isEmpty()) {
            throw new BusinessException("One or more images are already associated with a different raffle");
        }
    }

    @Override
    public void validateAllArePending(List<Image> images) {
        boolean anyLinked = images.stream().anyMatch(img -> img.getRaffle() != null);
        if (anyLinked) {
            throw new BusinessException("One or more images are already linked to a raffle.");
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