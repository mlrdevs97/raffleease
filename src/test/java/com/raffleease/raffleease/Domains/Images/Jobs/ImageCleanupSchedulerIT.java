package com.raffleease.raffleease.Domains.Images.Jobs;

import com.raffleease.raffleease.Base.BaseIT;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ImageCleanupSchedulerIT extends BaseIT {
    @Autowired
    private ImageCleanupScheduler scheduler;

    @Test
    public void testOrphanImageCleanupDeletesOldImages() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        ImageDTO image = images.get(0);

        Image savedImage = imagesRepository.findById(image.id()).orElseThrow();
        savedImage.setCreatedAt(LocalDateTime.now().minusDays(1));
        imagesRepository.save(savedImage);

        scheduler.cleanOrphanImages();

        assertThat(imagesRepository.findById(image.id())).isEmpty();
        assertThat(Files.exists(Paths.get(image.filePath()))).isFalse();
    }

    @Test
    public void testNoEligibleOrphanImagesResultsInNoDeletion() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        ImageDTO image = images.get(0);

        Image savedImage = imagesRepository.findById(image.id()).orElseThrow();
        savedImage.setCreatedAt(LocalDateTime.now().plusMinutes(1));
        imagesRepository.save(savedImage);

        scheduler.cleanOrphanImages();

        assertThat(imagesRepository.findById(image.id())).isPresent();
        assertThat(Files.exists(Paths.get(image.filePath()))).isTrue();
    }
}