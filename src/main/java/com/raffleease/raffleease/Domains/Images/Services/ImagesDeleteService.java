package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.Model.Image;

import java.util.List;

public interface ImagesDeleteService {
    /**
     * Deletes multiple images at once (used by cleanup scheduler)
     */
    void deleteAll(List<Image> images);
    
    /**
     * Deletes a single image (used internally for cleanup operations)
     */
    void delete(Image image);

    /**
     * Soft deletes an image by setting the status to MARKED_FOR_DELETION
     * Soft deleted images cannot be fetched by the API and will be deleted when raffle create/edit process completes
     */
    void softDelete(Long id);
}
