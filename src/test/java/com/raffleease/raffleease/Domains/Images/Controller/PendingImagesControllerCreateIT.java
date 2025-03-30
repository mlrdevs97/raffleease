package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class PendingImagesControllerCreateIT extends BaseImagesIT {
    @Value("${spring.storage.images.base_path}")
    private String basePath;

    @Test
    void shouldUploadImagesSuccessfullyAndStoreInExpectedPath() throws Exception {
        MvcResult result = uploadImages(2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New images created successfully"))
                .andExpect(jsonPath("$.data.images", hasSize(2)))
                .andReturn();

        List<ImageDTO> images = parseImagesFromResponse(result);

        String subject = tokensQueryService.getSubject(accessToken);
        Association association = associationsRepository.findById(Long.parseLong(subject)).orElseThrow();

        for (ImageDTO image : images) {
            assertThat(imagesRepository.findById(image.id())).isNotNull();

            Path expectedPath = Paths.get(basePath + "/associations/" + association.getId() + "/images/raffles/temp/" + image.id());
            assertThat(Files.exists(expectedPath)).isTrue();
        }
    }

    @Test
    void shouldFailWhenNoFilesAreProvided() throws Exception {
        uploadImages(0)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Must provide at least one image")));
    }

    @Test
    void shouldFailWhenMoreThanTenFilesAreProvided() throws Exception {
        uploadImages(11)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Must provide between 1 and 10 images")));
    }

    @Test
    void shouldFailWhenTotalImagesExceedLimit() throws Exception {
        uploadImages(10);
        uploadImages(1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot upload more than 10 images in total"));
    }
}