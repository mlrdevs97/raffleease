package com.raffleease.raffleease.Domains.Images.Mappers;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageResponse;
import com.raffleease.raffleease.Domains.Images.Model.Image;

import java.util.List;

public interface IImagesMapper {
    List<ImageResponse> fromImagesList(List<Image> images);
}
