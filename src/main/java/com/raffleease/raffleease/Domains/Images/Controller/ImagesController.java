package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;
import com.raffleease.raffleease.Domains.Images.Services.ImagesCreateService;
import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@ValidateAssociationAccess
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/associations/{associationId}/raffles/{raffleId}/images")
public class ImagesController {
    private final ImagesCreateService imagesCreateService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
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
}
