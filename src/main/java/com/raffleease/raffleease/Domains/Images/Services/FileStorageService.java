package com.raffleease.raffleease.Domains.Images.Services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.List;

public interface FileStorageService {
    Path moveFileToRaffle(String associationId, String raffleId, String imageId, String tempPath);
    Resource load(String filePath);
    void delete(String filePath);    
    List<String> saveTemporaryBatch(List<MultipartFile> files, String associationId, String batchId);
    List<String> moveTemporaryBatchToFinal(List<String> tempPaths, String associationId, String raffleId, List<String> imageIds);
    void cleanupTemporaryFiles(List<String> tempPaths);
    void cleanupFiles(List<String> filePaths);
}
