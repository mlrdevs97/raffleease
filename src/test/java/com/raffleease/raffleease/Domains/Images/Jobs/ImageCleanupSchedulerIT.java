package com.raffleease.raffleease.Domains.Images.Jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImageCleanupSchedulerIT extends BaseIT {
    @Autowired
    private ImagesRepository imagesRepository;

    @Value("${spring.storage.images.cleanup.cutoff_seconds}")
    private Long cutOffSeconds;

    @Test
    public void testOrphanImageCleanupDeletesOldImages() throws Exception {
        MockMultipartFile image1 = new MockMultipartFile("files", "image1.jpg", "image/jpeg", "testdata1".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/v1/images")
                        .file(image1)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New images created successfully"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode imageNode = responseJson.path("data").get(0);
        Long imageId = imageNode.path("id").asLong();
        String filePath = imageNode.path("filePath").asText();

        Thread.sleep(7000);

        assertThat(imagesRepository.findById(imageId)).isEmpty();
        assertThat(Files.exists(Paths.get(filePath))).isFalse();
    }

    @Test
    public void testNoEligibleOrphanImagesResultsInNoDeletion() throws Exception {
        MockMultipartFile image1 = new MockMultipartFile("files", "recent-image.jpg", "image/jpeg", "recent-testdata".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/v1/images")
                        .file(image1)
                        .header(AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New images created successfully"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode imageNode = responseJson.path("data").get(0);
        Long imageId = imageNode.path("id").asLong();
        String filePath = imageNode.path("filePath").asText();

        Image image = imagesRepository.findById(imageId).orElseThrow();
        image.setCreatedAt(LocalDateTime.now().plusMinutes(1));
        imagesRepository.save(image);

        Thread.sleep(7000);

        assertThat(imagesRepository.findById(imageId)).isPresent();
        assertThat(Files.exists(Paths.get(filePath))).isTrue();
    }
}