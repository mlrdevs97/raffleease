package com.raffleease.raffleease.Domains.Images.Controller;

import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.util.AuthTestUtils;
import com.raffleease.raffleease.util.AuthTestUtils.AuthTestData;
import com.raffleease.raffleease.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pending Images Controller Integration Tests")
class PendingImagesControllerIT extends AbstractIntegrationTest {

    @Autowired
    private AuthTestUtils authTestUtils;

    @Autowired
    private ImagesRepository imagesRepository;

    private AuthTestData authData;
    private String baseEndpoint;

    @BeforeEach
    void setUp() {
        authData = authTestUtils.createAuthenticatedUser();
        baseEndpoint = "/v1/associations/" + authData.association().getId() + "/images";
    }

    @Nested
    @DisplayName("POST /v1/associations/{associationId}/images")
    class UploadImagesTests {

        @Test
        @DisplayName("Should successfully upload single image when authenticated")
        void shouldUploadSingleImageWhenAuthenticated() throws Exception {
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
    }

    @Nested
    @DisplayName("DELETE /v1/associations/{associationId}/images/{id}")
    class DeleteImageTests {

        @Test
        @DisplayName("Should successfully delete pending image when authenticated")
        void shouldDeletePendingImageWhenAuthenticated() throws Exception {
            // Arrange
            Image pendingImage = TestDataBuilder.image()
                    .fileName("to-delete.jpg")
                    .association(authData.association())
                    .pendingImage()
                    .imageOrder(1)
                    .build();
            pendingImage = imagesRepository.save(pendingImage);

            // Act
            ResultActions result = mockMvc.perform(delete(baseEndpoint + "/" + pendingImage.getId())
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isNoContent());

            // Verify image was deleted
            Optional<Image> deletedImage = imagesRepository.findById(pendingImage.getId());
            assertThat(deletedImage).isEmpty();
        }

        @Test
        @DisplayName("Should update image orders after deletion")
        void shouldUpdateImageOrdersAfterDeletion() throws Exception {
            // Arrange - Create 3 pending images
            Image image1 = TestDataBuilder.image()
                    .fileName("image1.jpg")
                    .association(authData.association())
                    .pendingImage()
                    .imageOrder(1)
                    .build();
            Image image2 = TestDataBuilder.image()
                    .fileName("image2.jpg")
                    .association(authData.association())
                    .pendingImage()
                    .imageOrder(2)
                    .build();
            Image image3 = TestDataBuilder.image()
                    .fileName("image3.jpg")
                    .association(authData.association())
                    .pendingImage()
                    .imageOrder(3)
                    .build();

            image1 = imagesRepository.save(image1);
            image2 = imagesRepository.save(image2);
            image3 = imagesRepository.save(image3);

            // Act - Delete the middle image (order 2)
            ResultActions result = mockMvc.perform(delete(baseEndpoint + "/" + image2.getId())
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isNoContent());

            // Verify remaining images have updated orders
            List<Image> remainingImages = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
            assertThat(remainingImages).hasSize(2);
            
            Image updatedImage3 = imagesRepository.findById(image3.getId()).orElseThrow();
            assertThat(updatedImage3.getImageOrder()).isEqualTo(2); // Should be updated from 3 to 2
        }

        @Test
        @DisplayName("Should return 404 when image does not exist")
        void shouldReturn404WhenImageDoesNotExist() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(delete(baseEndpoint + "/99999")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Arrange
            Image pendingImage = TestDataBuilder.image()
                    .association(authData.association())
                    .pendingImage()
                    .build();
            pendingImage = imagesRepository.save(pendingImage);

            // Act
            ResultActions result = mockMvc.perform(delete(baseEndpoint + "/" + pendingImage.getId()));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when trying to delete image from different association")
        void shouldReturn403WhenImageBelongsToDifferentAssociation() throws Exception {
            // Arrange
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserWithCredentials(
                    "otheruser3", "other3@example.com", "password123");
            Image otherAssociationImage = TestDataBuilder.image()
                    .association(otherUserData.association())
                    .pendingImage()
                    .build();
            otherAssociationImage = imagesRepository.save(otherAssociationImage);

            // Act
            ResultActions result = mockMvc.perform(delete(baseEndpoint + "/" + otherAssociationImage.getId())
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You are not authorized to delete this image"));
        }
    }

    /**
     * Creates minimal test image content for testing purposes.
     */
    private byte[] createTestImageContent() {
        // Create a minimal valid JPEG-like content for testing
        return new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, // JPEG header
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, // JFIF marker
                0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00, // JFIF data
                (byte) 0xFF, (byte) 0xD9 // JPEG end marker
        };
    }
} 