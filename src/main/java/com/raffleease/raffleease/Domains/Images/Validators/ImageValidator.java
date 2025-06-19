package com.raffleease.raffleease.Domains.Images.Validators;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Users.Model.User;

import java.util.List;

public interface ImageValidator {
    <T> void validateNoDuplicates(List<T> list, String message);
    void validatePendingImagesBelongToUser(User user, List<Image> images);
    void validateTotalImagesNumber(long uploadingImagesCount, long currentImagesCount);
    void validatePendingImagesNotAssociatedWithRaffle(List<Image> images);
    void validateActiveImagesBelongToRaffle(Raffle raffle, List<Image> images);
    void validateAllArePending(List<Image> images);
    void validateAtLeastOneImage(List<Image> existingImages);
    void validateImagesBelongToAssociation(Association association, List<Image> existingImages);
}
