package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import org.springframework.core.io.Resource;

import java.util.List;

public interface ImagesService {
    List<Image> saveAll(List<Image> images);
    Image findById(Long id);
    List<Image> findAllById(List<Long> ids);
    Resource getFile(Long id);
}