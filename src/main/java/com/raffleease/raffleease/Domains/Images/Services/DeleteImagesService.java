package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface DeleteImagesService {
    void deleteImage(HttpServletRequest request, Long id);
    void deleteAll(List<Image> images);
}
