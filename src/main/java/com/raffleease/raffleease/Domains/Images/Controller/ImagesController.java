package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;
import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.Services.ImagesCreateService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesUpdateOrderService;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/raffles/{raffleId}/images")
public class ImagesController {
    private final ImagesDeleteService imagesDeleteService;
    private final ImagesCreateService imagesCreateService;
    private final ImagesUpdateOrderService imagesUpdateOrderService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadImages(
            HttpServletRequest request,
            @PathVariable Long raffleId,
            @Valid @ModelAttribute ImageUpload imageUpload
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        imagesCreateService.create(request, raffleId, imageUpload),
                        "New images created successfully"
                )
        );
    }

    @PostMapping("/order")
    public ResponseEntity<ApiResponse> updateImageOrder(
            HttpServletRequest request,
            @PathVariable Long raffleId,
            @RequestBody @Valid UpdateOrderRequest updateOrderRequest
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        imagesUpdateOrderService.updateImageOrderOnEdit(request, raffleId, updateOrderRequest),
                        "Image order updated successfully"
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        imagesDeleteService.deleteImage(request, id);
        return ResponseEntity.noContent().build();
    }
}
