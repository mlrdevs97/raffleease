package com.raffleease.raffleease.Domains.Images.DTOs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ImageUpload(
        @NotEmpty(message = "Must provide at least one image")
        @Size(min = 1, max = 10, message = "Must provide between 1 and 10 images")
        List<MultipartFile> files
) {
}
