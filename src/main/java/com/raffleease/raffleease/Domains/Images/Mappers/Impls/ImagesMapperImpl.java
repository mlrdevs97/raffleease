package com.raffleease.raffleease.Domains.Images.Mappers.Impls;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageFile;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;
import com.raffleease.raffleease.Domains.Images.Mappers.IImagesMapper;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Services.IImagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ImagesMapperImpl implements IImagesMapper {
    private final IImagesService imagesService;

    @Override
    public List<ImageResponse> fromImagesList(List<Image> images) {
        return images.stream().map(image -> {
            ImageFile file = imagesService.get(image);
            return ImageResponse.builder()
                    .id(image.getId())
                    .imageFile(file)
                    .originalName(image.getOriginalName())
                    .filePath(image.getFilePath())
                    .build();
        }).toList();
    }
}
