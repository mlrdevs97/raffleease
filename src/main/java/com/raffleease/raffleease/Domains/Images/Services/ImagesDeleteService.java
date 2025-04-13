package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.Model.Image;

import java.util.List;

public interface ImagesDeleteService {
    void deleteImage(Long associationId, Long id);
    void deleteAll(List<Image> images);
}
