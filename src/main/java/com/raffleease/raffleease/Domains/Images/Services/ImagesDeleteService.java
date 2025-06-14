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
}
