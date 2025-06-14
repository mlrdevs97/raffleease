package com.raffleease.raffleease.Domains.Images.Services;

import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Domains.Images.Jobs.FilesCleanupScheduler;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.util.AuthTestUtils;
import com.raffleease.raffleease.util.AuthTestUtils.AuthTestData;
import com.raffleease.raffleease.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Files Cleanup Scheduler Integration Tests")
@TestPropertySource(properties = {
    "spring.storage.images.base_path=target/test-storage"
})
class FileCleanupServiceIT extends AbstractIntegrationTest {

    @Autowired
    private FilesCleanupScheduler filesCleanupScheduler;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private AuthTestUtils authTestUtils;

    @Value("${spring.storage.images.base_path}")
    private String basePath;

    private AuthTestData authData;
    private Path testStoragePath;

    @BeforeEach
    void setUp() throws IOException {
        authData = authTestUtils.createAuthenticatedUser();
        testStoragePath = Paths.get(basePath);
        
        // Clean up test storage directory
        if (Files.exists(testStoragePath)) {
            deleteRecursively(testStoragePath);
        }
        Files.createDirectories(testStoragePath);
    }

    @Test
    @DisplayName("Should cleanup old temporary files older than specified duration")
    void shouldCleanupOldTemporaryFiles() throws IOException {
        // Arrange - Create temporary files with different ages
        Path tempDir = createTempDirectory();
        Path oldFile = createTempFile(tempDir, "old-temp-file.jpg");
        Path newFile = createTempFile(tempDir, "new-temp-file.jpg");
        
        // Make old file appear older by modifying last modified time
        Files.setLastModifiedTime(oldFile, 
            java.nio.file.attribute.FileTime.from(
                java.time.Instant.now().minus(Duration.ofHours(25))
            )
        );

        // Act
        int cleanedUpCount = filesCleanupScheduler.cleanupOldTemporaryFiles(Duration.ofHours(24));

        // Assert
        assertThat(cleanedUpCount).isGreaterThanOrEqualTo(1);
        assertThat(Files.exists(newFile)).isTrue();
        assertThat(Files.exists(oldFile)).isFalse(); // Old file should be deleted
    }

    @Test
    @DisplayName("Should cleanup orphaned files that exist on disk but not in database")
    void shouldCleanupOrphanedFiles() throws IOException {
        // Arrange - Create files on disk
        Path associationDir = Paths.get(basePath, "associations", 
            String.valueOf(authData.association().getId()), "images");
        Files.createDirectories(associationDir);
        
        Path orphanedFile = associationDir.resolve("orphaned-file.jpg");
        Path validFile = associationDir.resolve("valid-file.jpg");
        
        Files.write(orphanedFile, "test content".getBytes());
        Files.write(validFile, "test content".getBytes());
        
        // Create database entry for valid file only
        Image validImage = TestDataBuilder.image()
                .association(authData.association())
                .pendingImage()
                .filePath(validFile.toString())
                .fileName("valid-file.jpg")
                .build();
        imagesRepository.save(validImage);

        // Act
        int cleanedUpCount = filesCleanupScheduler.cleanupOrphanedFiles();

        // Assert
        assertThat(cleanedUpCount).isGreaterThanOrEqualTo(1);
        assertThat(Files.exists(validFile)).isTrue(); // File with DB entry should remain
        assertThat(Files.exists(orphanedFile)).isFalse(); // Orphaned file should be deleted
    }

    @Test
    @DisplayName("Should not cleanup files that exist in database")
    void shouldNotCleanupFilesInDatabase() throws IOException {
        // Arrange - Create file and corresponding database entry
        Path associationDir = Paths.get(basePath, "associations", 
            String.valueOf(authData.association().getId()), "images");
        Files.createDirectories(associationDir);
        
        Path validFile = associationDir.resolve("database-file.jpg");
        Files.write(validFile, "test content".getBytes());
        
        Image validImage = TestDataBuilder.image()
                .association(authData.association())
                .pendingImage()
                .filePath(validFile.toString())
                .fileName("database-file.jpg")
                .build();
        imagesRepository.save(validImage);

        // Act
        filesCleanupScheduler.cleanupOrphanedFiles();

        // Assert
        assertThat(Files.exists(validFile)).isTrue(); // File should remain
        
        // Verify database entry still exists
        List<Image> images = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
        assertThat(images).hasSize(1);
        assertThat(images.get(0).getFilePath()).isEqualTo(validFile.toString());
    }

    @Test
    @DisplayName("Should perform full cleanup including both temp and orphaned files")
    void shouldPerformFullCleanup() throws IOException {
        // Arrange - Create both temp and orphaned files
        Path tempDir = createTempDirectory();
        Path tempFile = createTempFile(tempDir, "temp-file.jpg");
        
        Path associationDir = Paths.get(basePath, "associations", 
            String.valueOf(authData.association().getId()), "images");
        Files.createDirectories(associationDir);
        Path orphanedFile = associationDir.resolve("orphaned-file.jpg");
        Files.write(orphanedFile, "test content".getBytes());

        // Act
        int totalCleaned = filesCleanupScheduler.performFullCleanup();

        // Assert
        assertThat(totalCleaned).isGreaterThanOrEqualTo(1);
        assertThat(Files.exists(orphanedFile)).isFalse();
    }

    @Test
    @DisplayName("Should handle gracefully when no files need cleanup")
    void shouldHandleGracefullyWhenNoFilesNeedCleanup() {
        // Act
        int tempCleaned = filesCleanupScheduler.cleanupOldTemporaryFiles(Duration.ofHours(24));
        int orphanedCleaned = filesCleanupScheduler.cleanupOrphanedFiles();
        int totalCleaned = filesCleanupScheduler.performFullCleanup();

        // Assert - Should not fail even when no files exist
        assertThat(tempCleaned).isGreaterThanOrEqualTo(0);
        assertThat(orphanedCleaned).isGreaterThanOrEqualTo(0);
        assertThat(totalCleaned).isGreaterThanOrEqualTo(0);
    }

    // Helper methods

    private Path createTempDirectory() throws IOException {
        Path tempDir = Paths.get(basePath, "associations", 
            String.valueOf(authData.association().getId()), "images", "temp");
        Files.createDirectories(tempDir);
        return tempDir;
    }

    private Path createTempFile(Path directory, String fileName) throws IOException {
        Path file = directory.resolve(fileName);
        Files.write(file, "test content".getBytes());
        return file;
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(subPath -> {
                try {
                    deleteRecursively(subPath);
                } catch (IOException e) {
                    // Ignore errors during cleanup
                }
            });
        }
        Files.deleteIfExists(path);
    }
} 