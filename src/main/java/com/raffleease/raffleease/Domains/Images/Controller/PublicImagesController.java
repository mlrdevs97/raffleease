package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/public/v1/associations/{associationId}")
public class PublicImagesController {
    private final ImagesService imagesService;

    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> getTemp(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok().body(imagesService.getFile(id));
    }

    @GetMapping("/raffles/{raffleId}/images/{id}")
    public ResponseEntity<Resource> get(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok().body(imagesService.getFile(id));
    }
}
