package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.DTOs.*;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface ImagesService {
    List<Image> saveAll(List<Image> images);
    Image findById(Long id);
    List<Image> findAllById(List<Long> ids);
    ImageFile getFile(Long id);
}