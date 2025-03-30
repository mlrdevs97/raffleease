package com.raffleease.raffleease.Domains.Images.Services.Impls;

import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Service
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${spring.storage.images.base_path}")
    private String basePath;

    @Override
    public String save(MultipartFile file, String associationId, String imageId) {
        try {
            String fileName = imageId + "_" + file.getOriginalFilename();
            Path directoryPath = Paths.get(basePath, "associations", associationId, "images", "raffles", "temp");
            Files.createDirectories(directoryPath);
            Path filePath = directoryPath.resolve(fileName);
            Files.write(filePath, file.getBytes());
            return filePath.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Failed to upload file: " + ex.getMessage());
        }
    }

    @Override
    public Path moveFileToRaffle(String associationId, String raffleId, String imageId, String tempPath) {
        try {
            Path tempFile = Paths.get(tempPath);
            String fileName = tempFile.getFileName().toString();
            Path finalDir = Paths.get(basePath, "associations", associationId, "raffles", raffleId, "images");
            Files.createDirectories(finalDir);
            Path finalFilePath = finalDir.resolve(fileName);
            Files.move(tempFile, finalFilePath);
            return finalFilePath;
        } catch (IOException e) {
            throw new FileStorageException("Failed to move file: " + e.getMessage());
        }
    }

    @Override
    public byte[] load(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException ex) {
            throw new FileStorageException("Failed to read file: " + ex.getMessage());
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ex) {
            throw new FileStorageException("Failed to delete file: " + ex.getMessage());
        }
    }
}