package com.raffleease.raffleease.Domains.Images.DTOs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.raffleease.raffleease.Constants.ImagesConstraints.MAX_IMAGES;
import static com.raffleease.raffleease.Constants.ImagesConstraints.MIN_IMAGES;

public record ImageUpload(
        @NotEmpty(message = "Must provide at least one image")
        @Size(min = MIN_IMAGES, max = MAX_IMAGES, message = "Must provide between 1 and 10 images")
        List<MultipartFile> files
) {
}
