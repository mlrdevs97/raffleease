package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageUpload;
import com.raffleease.raffleease.Domains.Images.Services.ImagesDeleteService;
import com.raffleease.raffleease.Domains.Images.Services.ImagesCreateService;
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
@RequestMapping("/api/v1/images")
public class PendingImagesController {
    private final ImagesService imagesService;
    private final ImagesCreateService imagesCreateService;
    private final ImagesUpdateOrderService imagesUpdateOrderService;
    private final ImagesDeleteService imagesDeleteService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadImages(
            HttpServletRequest request,
            @Valid @ModelAttribute ImageUpload imageUpload
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        imagesCreateService.create(request, imageUpload),
                        "New images created successfully"
                )
        );
    }

    @PostMapping("/order")
    public ResponseEntity<ApiResponse> updateImageOrder(
            HttpServletRequest request,
            @RequestBody @Valid UpdateOrderRequest updateOrderRequest
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        imagesUpdateOrderService.updateImageOrderOnCreate(request, updateOrderRequest),
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