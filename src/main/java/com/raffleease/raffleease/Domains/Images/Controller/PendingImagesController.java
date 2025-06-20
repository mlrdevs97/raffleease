package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;
import com.raffleease.raffleease.Domains.Images.Services.ImagesCreateService;
import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import com.raffleease.raffleease.Domains.Auth.Validations.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.MEMBER;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@ValidateAssociationAccess
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/associations/{associationId}/images")
public class PendingImagesController {
    private final ImagesDeleteService deleteService;
    private final ImagesCreateService createService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    @RequireRole(value = MEMBER, message = "Only administrators and members can upload images")
    public ResponseEntity<ApiResponse> upload(
            @PathVariable Long associationId,
            @Valid @ModelAttribute ImageUpload imageUpload
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        createService.create(associationId, imageUpload),
                        "New images created successfully"
                )
        );
    }

    @DeleteMapping("/{imageId}")
    @RequireRole(value = MEMBER, message = "Only administrators and members can delete images")
    public ResponseEntity<Void> delete(
            @PathVariable Long imageId
    ) {
        deleteService.softDelete(imageId);
        return ResponseEntity.noContent().build();
    }
}