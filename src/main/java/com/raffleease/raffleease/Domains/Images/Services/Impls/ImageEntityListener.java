package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Services.IFIleStorage;
import jakarta.persistence.PreRemove;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ImageEntityListener {
    private final IFIleStorage fileStorage;

    @PreRemove
    public void onPreRemove(Image image) {
        if (image.getFilePath() != null) {
            fileStorage.delete(image.getFilePath());
        }
    }
}
