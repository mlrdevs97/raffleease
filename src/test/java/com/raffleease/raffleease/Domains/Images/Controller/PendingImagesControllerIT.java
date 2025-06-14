package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pending Images Controller Integration Tests")
class PendingImagesControllerIT extends AbstractIntegrationTest {

    @Autowired
    private AuthTestUtils authTestUtils;

    @Autowired
    private ImagesRepository imagesRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    private AuthTestData authData;
    private String baseEndpoint;

    @BeforeEach
    void setUp() {
        authData = authTestUtils.createAuthenticatedUser();
        baseEndpoint = "/v1/associations/" + authData.association().getId() + "/images";
        
        // Mock new batch file storage methods for pending images (no raffle, so null raffleId)
        when(fileStorageService.saveTemporaryBatch(anyList(), anyString(), anyString()))
                .thenReturn(List.of("/mocked/temp/pending/path1"));
        
        when(fileStorageService.moveTemporaryBatchToFinal(anyList(), anyString(), isNull(), anyList()))
                .thenReturn(List.of("/mocked/pending/final/path1"));
    }

    @Nested
    @DisplayName("POST /v1/associations/{associationId}/images")
    class UploadImagesTests {

        @Test
        @DisplayName("Should successfully upload pending images when authenticated")
        void shouldUploadPendingImagesWhenAuthenticated() throws Exception {
            // Arrange
            MockMultipartFile imageFile = new MockMultipartFile(
                    "files", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    createTestImageContent()
            );

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(imageFile)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("New images created successfully"))
                    .andExpect(jsonPath("$.data.images").isArray())
                    .andExpect(jsonPath("$.data.images[0].fileName").value("test-image.jpg"))
                    .andExpect(jsonPath("$.data.images[0].contentType").value("image/jpeg"))
                    .andExpect(jsonPath("$.data.images[0].url").exists())
                    .andExpect(jsonPath("$.data.images[0].imageOrder").value(1));

            // Verify database state
            List<Image> savedImages = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
            assertThat(savedImages).hasSize(1);
            assertThat(savedImages.get(0).getFileName()).isEqualTo("test-image.jpg");
            assertThat(savedImages.get(0).getAssociation().getId()).isEqualTo(authData.association().getId());
            assertThat(savedImages.get(0).getRaffle()).isNull(); // Pending image
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Arrange
            MockMultipartFile imageFile = new MockMultipartFile(
                    "files", 
                    "test.jpg", 
                    "image/jpeg", 
                    createTestImageContent()
            );

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(imageFile));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when user doesn't belong to association")
        void shouldReturn403WhenUserDoesntBelongToAssociation() throws Exception {
            // Arrange
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserWithCredentials(
                    "otheruser", "other@example.com", "password123");
            MockMultipartFile imageFile = new MockMultipartFile(
                    "files", 
                    "test.jpg", 
                    "image/jpeg", 
                    createTestImageContent()
            );

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(imageFile)
                    .with(user(otherUserData.user().getEmail())));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 when files exceed existing pending images limit")
        void shouldReturn400WhenExceedingImageLimit() throws Exception {
            // Arrange - Create 8 existing pending images
            for (int i = 0; i < 8; i++) {
                Image existingImage = TestDataBuilder.image()
                        .fileName("existing" + i + ".jpg")
                        .association(authData.association())
                        .pendingImage()
                        .imageOrder(i + 1)
                        .build();
                imagesRepository.save(existingImage);
            }

            // Try to upload 3 more files separately to simulate exceeding limit
            MockMultipartFile image1 = new MockMultipartFile("files", "new1.jpg", "image/jpeg", createTestImageContent());
            MockMultipartFile image2 = new MockMultipartFile("files", "new2.jpg", "image/jpeg", createTestImageContent());
            MockMultipartFile image3 = new MockMultipartFile("files", "new3.jpg", "image/jpeg", createTestImageContent());

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(image1)
                    .file(image2)
                    .file(image3)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You cannot upload more than 10 images in total"));
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
        @DisplayName("Should not create database records when file storage batch save fails")
        void shouldNotCreateDatabaseRecordsWhenFileStorageBatchSaveFails() throws Exception {
            // Arrange - Create existing pending images to get close to limit
            for (int i = 0; i < 9; i++) {
                Image existingImage = TestDataBuilder.image()
                        .fileName("existing" + i + ".jpg")
                        .association(authData.association())
                        .pendingImage()
                        .imageOrder(i + 1)
                        .build();
                imagesRepository.save(existingImage);
            }
            
            // Reset and reconfigure mock to throw exception after validation passes
            reset(fileStorageService);
            when(fileStorageService.saveTemporaryBatch(anyList(), anyString(), anyString()))
                    .thenThrow(new FileStorageException("Simulated batch storage failure"));

            MockMultipartFile imageFile = new MockMultipartFile(
                    "files", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    createTestImageContent()
            );

            // Count images before the operation
            List<Image> imagesBefore = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
            int countBefore = imagesBefore.size();

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(imageFile)
                    .with(user(authData.user().getEmail())));

            // Assert - Operation should fail completely
            result.andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Simulated batch storage failure"))
                    .andExpect(jsonPath("$.code").value("FILE_STORAGE_ERROR"));

            // Verify database state is unchanged - no new records should be created
            List<Image> imagesAfter = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
            assertThat(imagesAfter).hasSize(countBefore);
            
            // Verify the specific file was not saved
            List<Image> allImages = imagesRepository.findAll();
            assertThat(allImages.stream().anyMatch(img -> "test-image.jpg".equals(img.getFileName()))).isFalse();
        }

        @Test
        @DisplayName("Should not create database records when file move batch fails")
        void shouldNotCreateDatabaseRecordsWhenFileMoveBatchFails() throws Exception {
            // Arrange - Reset and reconfigure mock to throw exception during move phase
            reset(fileStorageService);
            when(fileStorageService.saveTemporaryBatch(anyList(), anyString(), anyString()))
                    .thenReturn(List.of("/mocked/temp/path"));
            when(fileStorageService.moveTemporaryBatchToFinal(anyList(), anyString(), isNull(), anyList()))
                    .thenThrow(new FileStorageException("Simulated batch move failure"));

            MockMultipartFile imageFile = new MockMultipartFile(
                    "files", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    createTestImageContent()
            );

            // Count images before the operation
            List<Image> imagesBefore = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
            int countBefore = imagesBefore.size();

            // Act
            ResultActions result = mockMvc.perform(multipart(baseEndpoint)
                    .file(imageFile)
                    .with(user(authData.user().getEmail())));

            // Assert - Operation should fail completely
            result.andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Simulated batch move failure"))
                    .andExpect(jsonPath("$.code").value("FILE_STORAGE_ERROR"));

            // Verify database state is unchanged - no new records should be created
            List<Image> imagesAfter = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
            assertThat(imagesAfter).hasSize(countBefore);
            
            // Verify the specific file was not saved
            List<Image> allImages = imagesRepository.findAll();
            assertThat(allImages.stream().anyMatch(img -> "test-image.jpg".equals(img.getFileName()))).isFalse();
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