package com.raffleease.raffleease.Domains.Images.Validators;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;

import java.util.List;

public interface ImageValidator {
    <T> void validateNoDuplicates(List<T> list, String message);
    void validateImagesBelongToAssociation(Association association, List<Image> images);
    void validateConsecutiveOrders(List<Integer> orders);
    void validateTotalImagesNumber(long uploadingImagesCount, long currentImagesCount);
    void validateAllImagesExist(List<Long> requestedIds, List<Image> foundImages);
    void validateImagesArePendingOrBelongToRaffle(Raffle raffle, List<Image> images);
    void validateAllArePending(List<Image> images);
}
