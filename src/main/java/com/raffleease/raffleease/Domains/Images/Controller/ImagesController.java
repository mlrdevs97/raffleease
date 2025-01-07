package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Images.Services.IImagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
@RestController
public class ImagesController {
    private final IImagesService imagesService;

    @DeleteMapping
    public ResponseEntity<Void> release(
            @PathVariable Long id
    ) {
        imagesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
