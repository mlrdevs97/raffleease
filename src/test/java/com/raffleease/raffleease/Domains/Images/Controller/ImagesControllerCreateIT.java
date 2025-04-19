package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImagesControllerCreateIT extends BaseImagesIT {
    @Value("${spring.storage.images.base_path}")
    private String basePath;
    private Raffle raffle;
    private List<ImageDTO> originalImages;

    @BeforeEach
    void setUp() throws Exception {
        originalImages = parseImagesFromResponse(uploadImages(2).andReturn());
        Long raffleId = createRaffle(originalImages, associationId, accessToken);
        raffle = rafflesRepository.findById(raffleId).orElseThrow();
    }

    @Test
    void shouldUploadAndStoreImages() throws Exception {
        MvcResult result = uploadImagesForRaffle(2, raffle.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New images created successfully"))
                .andExpect(jsonPath("$.data.images", hasSize(2)))
                .andReturn();

        List<ImageDTO> images = parseImagesFromResponse(result);

        String subject = tokensQueryService.getSubject(accessToken);
        Association association = associationsRepository.findById(Long.parseLong(subject)).orElseThrow();

        for (int i = 0; i < images.size(); i++) {
            ImageDTO image = images.get(i);

            assertThat(imagesRepository.findById(image.id())).isNotNull();

            assertThat(image.url()).contains("/api/v1/associations/" + associationId + "/raffles/" + raffle.getId() +  "/images/" + image.id());

            Path expectedPath = Paths.get(basePath + "/associations/" + association.getId() + "/images/raffles/temp/");
            assertThat(image.filePath()).contains(expectedPath.toString());
            assertThat(Files.exists(expectedPath)).isTrue();

            assertThat(image.imageOrder()).isEqualTo(originalImages.size() + i + 1);
        }
    }

    @Test
    void shouldUploadAndStoreImagesInVariousRequests() throws Exception {
        List<ImageDTO> firstImagesUpload = parseImagesFromResponse(uploadImagesForRaffle(2, raffle.getId()).andReturn());
        List<ImageDTO> secondImagesUpload = parseImagesFromResponse(uploadImagesForRaffle(2, raffle.getId()).andReturn());
        List<ImageDTO> allImages = Stream.concat(firstImagesUpload.stream(), secondImagesUpload.stream()).toList();

        String subject = tokensQueryService.getSubject(accessToken);
        Association association = associationsRepository.findById(Long.parseLong(subject)).orElseThrow();

        for (int i = 0; i < allImages.size(); i++) {
            ImageDTO image = allImages.get(i);

            assertThat(imagesRepository.findById(image.id())).isNotNull();
            assertThat(image.url()).contains("/api/v1/associations/" + associationId + "/raffles/" + raffle.getId() +  "/images/" + image.id());

            Path expectedPath = Paths.get(basePath + "/associations/" + association.getId() + "/images/raffles/temp/");
            assertThat(image.filePath()).contains(expectedPath.toString());
            assertThat(Files.exists(expectedPath)).isTrue();
            assertThat(image.imageOrder()).isEqualTo(originalImages.size() + i + 1);
        }
    }

    @Test
    void shouldFailWhenTotalImagesExceedLimit() throws Exception {
        uploadImagesForRaffle(9, raffle.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot upload more than 10 images in total"));
    }
}
