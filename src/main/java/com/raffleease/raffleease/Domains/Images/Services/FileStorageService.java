package com.raffleease.raffleease.Domains.Images.Services;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public interface FileStorageService {
    String save(MultipartFile file, String associationId, String imageId);
    Path moveFileToRaffle(String associationId, String raffleId, String imageId, String tempPath);
    byte[] load(String filePath);
    void delete(String filePath);
}
