package com.raffleease.raffleease.Domains.Images.Mappers.Impls;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Mappers.ImagesMapper;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ImagesMapperImpl implements ImagesMapper {
    @Override
    public List<ImageDTO> fromImagesList(List<Image> images) {
        return images.stream()
                .map(image -> ImageDTO.builder()
                        .id(image.getId())
                        .fileName(image.getFileName())
                        .filePath(image.getFilePath())
                        .contentType(image.getContentType())
                        .url(image.getUrl())
                        .imageOrder(image.getImageOrder())
                        .build()
                ).toList();
    }

    @Override
    public List<Image> toImagesList(Raffle raffle, List<ImageDTO> images) {
        return images.stream().map(imageDTO -> Image.builder()
                .id(imageDTO.id())
                .fileName(imageDTO.fileName())
                .filePath(imageDTO.filePath())
                .contentType(imageDTO.contentType())
                .url(imageDTO.url())
                .imageOrder(imageDTO.imageOrder())
                .raffle(raffle)
                .build()
        ).collect(Collectors.toList());
    }
}
