package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;
import com.raffleease.raffleease.Domains.Images.Services.ImagesCreateService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@ValidateAssociationAccess
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/associations/{associationId}/raffles/{raffleId}/images")
public class ImagesController {
    private final ImagesService imagesService;
    private final ImagesDeleteService imagesDeleteService;
    private final ImagesCreateService imagesCreateService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    @ValidateAssociationAccess
    public ResponseEntity<ApiResponse> uploadImages(
            @PathVariable Long associationId,
            @PathVariable Long raffleId,
            @Valid @ModelAttribute ImageUpload imageUpload
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        imagesCreateService.create(associationId, raffleId, imageUpload),
                        "New images created successfully"
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long associationId,
            @PathVariable Long id
    ) {
        imagesDeleteService.deleteImage(associationId, id);
        return ResponseEntity.noContent().build();
    }
}
