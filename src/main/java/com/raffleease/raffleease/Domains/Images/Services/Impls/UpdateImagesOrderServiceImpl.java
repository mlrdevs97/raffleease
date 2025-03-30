package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;
import com.raffleease.raffleease.Domains.Images.Mappers.ImagesMapper;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Validators.ImageValidator;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Domains.Images.Services.UpdateImagesOrderService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Services.RafflesPersistenceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UpdateImagesOrderServiceImpl implements UpdateImagesOrderService {
    private final AssociationsService associationsService;
    private final RafflesPersistenceService rafflesPersistenceService;
    private final ImagesMapper mapper;
    private final ImagesService imagesService;
    private final ImageValidator imageValidator;

    @Override
    public ImageResponse updateImageOrderOnCreate(HttpServletRequest request, UpdateOrderRequest updateOrderRequest) {
        return updateImageOrder(
                request,
                updateOrderRequest.images(),
                null,
                images -> imageValidator.validateAllArePending(images)
        );
    }

    @Override
    public ImageResponse updateImageOrderOnEdit(HttpServletRequest request, Long raffleId, UpdateOrderRequest updateOrderRequest) {
        Raffle raffle = rafflesPersistenceService.findById(raffleId);
        return updateImageOrder(
                request,
                updateOrderRequest.images(),
                raffle,
                images -> imageValidator.validateImagesArePendingOrBelongToRaffle(raffle, images)
        );
    }

    private ImageResponse updateImageOrder(
            HttpServletRequest request,
            List<ImageDTO> imageDTOs,
            Raffle raffle,
            Consumer<List<Image>> extraValidation
    ) {
        List<Long> imageIds = imageDTOs.stream().map(ImageDTO::id).toList();
        List<Integer> imageOrders = imageDTOs.stream().map(ImageDTO::imageOrder).toList();

        imageValidator.validateNoDuplicates(imageIds, "Duplicate image IDs found in request");
        imageValidator.validateNoDuplicates(imageOrders, "Duplicate image orders detected");
        imageValidator.validateConsecutiveOrders(imageOrders);

        Association association = associationsService.findFromRequest(request);
        List<Image> images = imagesService.findAllById(imageIds);
        imageValidator.validateAllImagesExist(imageIds, images);
        imageValidator.validateImagesBelongToAssociation(association, images);

        extraValidation.accept(images);

        Map<Long, Integer> orderMap = imageDTOs.stream()
                .collect(Collectors.toMap(ImageDTO::id, ImageDTO::imageOrder));

        for (Image image : images) {
            image.setImageOrder(orderMap.get(image.getId()));
        }

        List<Image> saved = imagesService.saveAll(images);

        List<ImageDTO> ordered = mapper.fromImagesList(saved).stream()
                .sorted(Comparator.comparing(ImageDTO::imageOrder))
                .toList();

        return new ImageResponse(ordered);
    }
}
