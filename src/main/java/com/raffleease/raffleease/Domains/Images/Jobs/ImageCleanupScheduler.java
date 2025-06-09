package com.raffleease.raffleease.Domains.Images.Jobs;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageCleanupScheduler {
    private final ImagesDeleteService imagesDeleteService;
    private final ImagesRepository imagesRepository;

    @Value("${spring.application.configs.cleanup.images_cleanup_cutoff_seconds}")
    private Long cutoffSeconds;

    @Scheduled(cron = "${spring.application.configs.cron.images_cleanup}")
    public void cleanOrphanImages() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(cutoffSeconds);
        List<Image> oldOrphanImages = imagesRepository.findAllByRaffleIsNullAndCreatedAtBefore(cutoff);
        if (oldOrphanImages.isEmpty()) return;
        imagesDeleteService.deleteAll(oldOrphanImages);
    }
}
