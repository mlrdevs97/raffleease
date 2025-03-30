package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Domains.Images.DTOs.UpdateOrderRequest;
import com.raffleease.raffleease.Domains.Images.Services.ImagesService;
import com.raffleease.raffleease.Domains.Images.Services.UpdateImagesOrderService;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/raffles/{raffleId}/images")
public class ImagesController {
    private final ImagesService imagesService;
    private final UpdateImagesOrderService updateImagesOrderService;

    @PostMapping("/order")
    public ResponseEntity<ApiResponse> updateImageOrder(
            HttpServletRequest request,
            @PathVariable Long raffleId,
            @RequestBody @Valid UpdateOrderRequest updateOrderRequest
    ) {
        return ResponseEntity.ok(
                ResponseFactory.success(
                        updateImagesOrderService.updateImageOrderOnEdit(request, raffleId, updateOrderRequest),
                        "Image order updated successfully"
                )
        );
    }
}
