package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.util.AuthTestUtils;
import com.raffleease.raffleease.util.AuthTestUtils.AuthTestData;
import com.raffleease.raffleease.util.TestDataBuilder;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Images Controller Integration Tests")
class ImagesControllerIT extends AbstractIntegrationTest {

    @Autowired
    private AuthTestUtils authTestUtils;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private RafflesRepository rafflesRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    private AuthTestData authData;
    private Raffle testRaffle;
    private String baseEndpoint;

    @BeforeEach
    void setUp() {
        authData = authTestUtils.createAuthenticatedUser();
        
        // Create a test raffle
        testRaffle = TestDataBuilder.raffle()
                .association(authData.association())
                .status(RaffleStatus.PENDING)
                .title("Test Raffle for Images")
                .build();
        testRaffle = rafflesRepository.save(testRaffle);
        
        baseEndpoint = "/v1/associations/" + authData.association().getId() + "/raffles/" + testRaffle.getId() + "/images";
        
        // Mock new batch file storage methods
        when(fileStorageService.saveTemporaryBatch(anyList(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    List<MultipartFile> files = invocation.getArgument(0);
                    return files.stream()
                            .map(file -> "/mocked/temp/" + file.getOriginalFilename())
                            .toList();
                });
        
        when(fileStorageService.moveTemporaryBatchToFinal(anyList(), anyString(), anyString(), anyList()))
                .thenAnswer(invocation -> {
                    List<String> tempPaths = invocation.getArgument(0);
                    return tempPaths.stream()
                            .map(path -> path.replace("/temp/", "/final/"))
                            .toList();
                });
    }

    @Nested
    @DisplayName("POST /v1/associations/{associationId}/raffles/{raffleId}/images")
    class UploadImagesTests {

        @Test
        @DisplayName("Should successfully upload images to existing raffle")
        void shouldUploadImagesToExistingRaffle() throws Exception {
            // Arrange
            MockMultipartFile file1 = new MockMultipartFile(
                    "files", "raffle-image-1.jpg", "image/jpeg", createTestImageContent());
            MockMultipartFile file2 = new MockMultipartFile(
                    "files", "raffle-image-2.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(file1)
                    .file(file2)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("New images created successfully"))
                    .andExpect(jsonPath("$.data.images").isArray())
                    .andExpect(jsonPath("$.data.images", hasSize(2)))
                    .andExpect(jsonPath("$.data.images[0].fileName").value("raffle-image-1.jpg"))
                    .andExpect(jsonPath("$.data.images[1].fileName").value("raffle-image-2.jpg"));

            // Verify database state
            List<Image> savedImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(savedImages).hasSize(2);

            // Verify images are properly associated to raffle
            for (Image image : savedImages) {
                assertThat(image.getRaffle()).isEqualTo(testRaffle);
                assertThat(image.getAssociation()).isEqualTo(authData.association());
                assertThat(image.getUrl()).contains("/raffles/" + testRaffle.getId() + "/images/");
                assertThat(image.getImageOrder()).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("Should set correct image order when adding to raffle with existing images")
        void shouldSetCorrectImageOrderWhenAddingToRaffleWithExistingImages() throws Exception {
            // Arrange - Create existing images for the raffle
            Image existingImage1 = TestDataBuilder.image()
                    .association(authData.association())
                    .raffle(testRaffle)
                    .imageOrder(1)
                    .fileName("existing-1.jpg")
                    .build();
            Image existingImage2 = TestDataBuilder.image()
                    .association(authData.association())
                    .raffle(testRaffle)
                    .imageOrder(2)
                    .fileName("existing-2.jpg")
                    .build();
            testRaffle.getImages().add(existingImage1);
            testRaffle.getImages().add(existingImage2);
            rafflesRepository.save(testRaffle);

            MockMultipartFile newFile = new MockMultipartFile(
                    "files", "new-image.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(newFile)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.images", hasSize(1)))
                    .andExpect(jsonPath("$.data.images[0].imageOrder").value(3));

            // Verify database state
            List<Image> allRaffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(allRaffleImages).hasSize(3);
            
            Optional<Image> newImage = allRaffleImages.stream()
                    .filter(img -> "new-image.jpg".equals(img.getFileName()))
                    .findFirst();
            assertThat(newImage).isPresent();
            assertThat(newImage.get().getImageOrder()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "files", "test.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(file));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when user doesn't belong to association")
        void shouldReturn403WhenUserDoesntBelongToAssociation() throws Exception {
            // Arrange
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserWithCredentials(
                    "otheruser", "other@example.com", "password123");
            MockMultipartFile file = new MockMultipartFile(
                    "files", "test.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(file)
                    .with(user(otherUserData.user().getEmail())));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when raffle doesn't exist")
        void shouldReturn404WhenRaffleDoesntExist() throws Exception {
            // Arrange
            String nonExistentRaffleEndpoint = "/v1/associations/" + authData.association().getId() + "/raffles/99999/images";
            MockMultipartFile file = new MockMultipartFile(
                    "files", "test.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(nonExistentRaffleEndpoint)
                    .file(file)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when no files are provided")
        void shouldReturn400WhenNoFilesProvided() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when uploading to raffle that already has 10 images")
        void shouldReturn400WhenRaffleAlreadyHas10Images() throws Exception {
            // Arrange - Create a raffle with 10 existing images
            List<Image> existingImages = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Image image = TestDataBuilder.image()
                        .association(authData.association())
                        .raffle(testRaffle)
                        .imageOrder(i + 1)
                        .fileName("existing-" + (i + 1) + ".jpg")
                        .build();
                existingImages.add(image);
            }
            testRaffle.setImages(existingImages);
            rafflesRepository.save(testRaffle);

            MockMultipartFile newFile = new MockMultipartFile(
                    "files", "new-image.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(newFile)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You cannot upload more than 10 images in total"));

            // Verify no new images were added
            List<Image> allRaffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(allRaffleImages).hasSize(10);
        }

        @Test
        @DisplayName("Should return 400 when total images exceed limit including pending images")
        void shouldReturn400WhenTotalImagesExceedLimitIncludingPendingImages() throws Exception {
            // Arrange - Create 5 pending images for the association
            List<Image> pendingImages = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Image pendingImage = TestDataBuilder.image()
                        .association(authData.association())
                        .pendingImage()
                        .imageOrder(i + 1)
                        .fileName("pending-" + (i + 1) + ".jpg")
                        .build();
                pendingImages.add(pendingImage);
            }
            imagesRepository.saveAll(pendingImages);

            // Create 3 existing images in the raffle
            List<Image> raffleImages = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Image raffleImage = TestDataBuilder.image()
                        .association(authData.association())
                        .raffle(testRaffle)
                        .imageOrder(i + 1)
                        .fileName("raffle-" + (i + 1) + ".jpg")
                        .build();
                raffleImages.add(raffleImage);
            }
            testRaffle.setImages(raffleImages);
            rafflesRepository.save(testRaffle);

            // Try to upload 3 more files (5 pending + 3 raffle + 3 new = 11 > 10 limit)
            MockMultipartFile file1 = new MockMultipartFile("files", "new1.jpg", "image/jpeg", createTestImageContent());
            MockMultipartFile file2 = new MockMultipartFile("files", "new2.jpg", "image/jpeg", createTestImageContent());
            MockMultipartFile file3 = new MockMultipartFile("files", "new3.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(file1)
                    .file(file2)
                    .file(file3)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You cannot upload more than 10 images in total"));
        }

        @Test
        @DisplayName("Should successfully upload when total is exactly at limit")
        void shouldSuccessfullyUploadWhenTotalIsExactlyAtLimit() throws Exception {
            // Arrange - Create 7 pending images + 2 raffle images = 9 total
            for (int i = 0; i < 7; i++) {
                Image pendingImage = TestDataBuilder.image()
                        .association(authData.association())
                        .pendingImage()
                        .imageOrder(i + 1)
                        .fileName("pending-" + (i + 1) + ".jpg")
                        .build();
                imagesRepository.save(pendingImage);
            }

            List<Image> raffleImages = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Image raffleImage = TestDataBuilder.image()
                        .association(authData.association())
                        .raffle(testRaffle)
                        .imageOrder(i + 1)
                        .fileName("raffle-" + (i + 1) + ".jpg")
                        .build();
                raffleImages.add(raffleImage);
            }
            testRaffle.setImages(raffleImages);
            rafflesRepository.save(testRaffle);

            // Upload 1 more file to reach exactly 10
            MockMultipartFile newFile = new MockMultipartFile(
                    "files", "final-image.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(newFile)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.images", hasSize(1)))
                    .andExpect(jsonPath("$.data.images[0].imageOrder").value(10));

            // Verify database state
            List<Image> allRaffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(allRaffleImages).hasSize(3);
        }

        @Test
        @DisplayName("Should fail entire operation when file storage batch save fails")
        void shouldFailEntireOperationWhenFileStorageBatchSaveFails() throws Exception {
            // Arrange - Mock batch storage to throw exception
            when(fileStorageService.saveTemporaryBatch(anyList(), anyString(), anyString()))
                    .thenThrow(new FileStorageException("Simulated batch storage failure"));

            MockMultipartFile file = new MockMultipartFile(
                    "files", "test-image.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(file)
                    .with(user(authData.user().getEmail())));

            // Assert - Operation should fail completely
            result.andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Simulated batch storage failure"))
                    .andExpect(jsonPath("$.code").value("FILE_STORAGE_ERROR"));

            // Verify no database records were created due to transaction rollback
            List<Image> savedImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(savedImages).isEmpty();
            
            // Verify the transaction was properly rolled back - no orphaned records
            List<Image> allImages = imagesRepository.findAll();
            // Should only contain any pre-existing images, not the failed upload
            assertThat(allImages.stream().anyMatch(img -> "test-image.jpg".equals(img.getFileName()))).isFalse();
        }

        @Test
        @DisplayName("Should fail entire operation when file move batch fails")
        void shouldFailEntireOperationWhenFileMoveBatchFails() throws Exception {
            // Arrange - Mock batch move to throw exception
            reset(fileStorageService);
            when(fileStorageService.saveTemporaryBatch(anyList(), anyString(), anyString()))
                    .thenReturn(List.of("/mocked/temp/path"));
            when(fileStorageService.moveTemporaryBatchToFinal(anyList(), anyString(), anyString(), anyList()))
                    .thenThrow(new FileStorageException("Simulated batch move failure"));

            MockMultipartFile file = new MockMultipartFile(
                    "files", "test-image.jpg", "image/jpeg", createTestImageContent());

            // Count images before the operation
            List<Image> imagesBefore = imagesRepository.findAllByRaffle(testRaffle);
            int countBefore = imagesBefore.size();

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(file)
                    .with(user(authData.user().getEmail())));

            // Assert - Operation should fail completely
            result.andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Simulated batch move failure"))
                    .andExpect(jsonPath("$.code").value("FILE_STORAGE_ERROR"));

            // Verify database state is unchanged - no new records should be created
            List<Image> imagesAfter = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(imagesAfter).hasSize(countBefore);
            
            // Verify the specific file was not saved
            List<Image> allImages = imagesRepository.findAll();
            assertThat(allImages.stream().anyMatch(img -> "test-image.jpg".equals(img.getFileName()))).isFalse();
        }

        @Test
        @DisplayName("Should properly calculate image order with mixed pending and raffle images")
        void shouldProperlyCalculateImageOrderWithMixedImages() throws Exception {
            // Arrange - Create complex scenario with both pending and raffle images
            // 3 pending images for association
            for (int i = 0; i < 3; i++) {
                Image pendingImage = TestDataBuilder.image()
                        .association(authData.association())
                        .pendingImage()
                        .imageOrder(i + 1)
                        .fileName("pending-" + (i + 1) + ".jpg")
                        .build();
                imagesRepository.save(pendingImage);
            }

            // 4 existing images in raffle
            List<Image> raffleImages = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Image raffleImage = TestDataBuilder.image()
                        .association(authData.association())
                        .raffle(testRaffle)
                        .imageOrder(i + 1)
                        .fileName("raffle-" + (i + 1) + ".jpg")
                        .build();
                raffleImages.add(raffleImage);
            }
            testRaffle.setImages(raffleImages);
            rafflesRepository.save(testRaffle);

            // Upload 2 new files
            MockMultipartFile file1 = new MockMultipartFile("files", "new1.jpg", "image/jpeg", createTestImageContent());
            MockMultipartFile file2 = new MockMultipartFile("files", "new2.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(file1)
                    .file(file2)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.images", hasSize(2)))
                    .andExpect(jsonPath("$.data.images[0].imageOrder").value(8)) // 3 pending + 4 raffle + 1 = 8
                    .andExpect(jsonPath("$.data.images[1].imageOrder").value(9)); // 3 pending + 4 raffle + 2 = 9

            // Verify database state
            List<Image> allRaffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(allRaffleImages).hasSize(6); // 4 existing + 2 new

            // Verify the new images have correct order
            List<Image> newImages = allRaffleImages.stream()
                    .filter(img -> img.getFileName().startsWith("new"))
                    .sorted((a, b) -> a.getImageOrder().compareTo(b.getImageOrder()))
                    .toList();
            
            assertThat(newImages).hasSize(2);
            assertThat(newImages.get(0).getImageOrder()).isEqualTo(8);
            assertThat(newImages.get(1).getImageOrder()).isEqualTo(9);
        }
    }

    private byte[] createTestImageContent() {
        // Create a simple test image content (mock JPEG header)
        return new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
                0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xD9 // End of JPEG marker
        };
    }
} 