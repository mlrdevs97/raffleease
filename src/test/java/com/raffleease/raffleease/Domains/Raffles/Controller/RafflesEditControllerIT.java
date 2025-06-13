package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Images.Services.FileStorageService;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Raffles.Model.CompletionReason;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatistics;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.util.AuthTestUtils;
import com.raffleease.raffleease.util.AuthTestUtils.AuthTestData;
import com.raffleease.raffleease.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Raffles Edit Controller Integration Tests")
class RafflesEditControllerIT extends AbstractIntegrationTest {

    @Autowired
    private AuthTestUtils authTestUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RafflesRepository rafflesRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private TicketsRepository ticketsRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    private AuthTestData authData;
    private String baseEndpoint;
    private Raffle testRaffle;

    @BeforeEach
    void setUp() {
        authData = authTestUtils.createAuthenticatedUser();
        
        // Create a test raffle
        testRaffle = TestDataBuilder.raffle()
                .association(authData.association())
                .status(RaffleStatus.PENDING)
                .title("Original Raffle Title")
                .description("Original description")
                .build();
        testRaffle = rafflesRepository.save(testRaffle);
        
        baseEndpoint = "/v1/associations/" + authData.association().getId() + "/raffles/" + testRaffle.getId();
        
        // Mock file storage service for file movement operations
        when(fileStorageService.moveFileToRaffle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Paths.get("/mocked/raffle/image/path"));
    }

    @Nested
    @DisplayName("PUT /v1/associations/{associationId}/raffles/{id}")
    class EditRaffleTests {

        @Test
        @DisplayName("Should successfully edit raffle basic information")
        void shouldEditRaffleBasicInformation() throws Exception {
            // Arrange
            RaffleEdit raffleEdit = new RaffleEdit(
                    "Updated Raffle Title",
                    "Updated description",
                    null, 
                    null,   
                    null,
                    null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Raffle edited successfully"))
                    .andExpect(jsonPath("$.data.title").value("Updated Raffle Title"))
                    .andExpect(jsonPath("$.data.description").value("Updated description"));

            // Verify database state
            Raffle updatedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(updatedRaffle.getTitle()).isEqualTo("Updated Raffle Title");
            assertThat(updatedRaffle.getDescription()).isEqualTo("Updated description");
            assertThat(updatedRaffle.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should successfully edit raffle with new images from pending images")
        void shouldEditRaffleWithNewImagesFromPendingImages() throws Exception {
            // Arrange - Create pending images
            List<Image> pendingImages = createPendingImagesForAssociation(3);
            List<ImageDTO> imageDTOs = convertToImageDTOs(pendingImages);

            RaffleEdit raffleEdit = new RaffleEdit(
                    null,
                    null,
                    null,
                    imageDTOs,
                    null,
                    null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.images", hasSize(3)));

            // Verify database state
            List<Image> raffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(raffleImages).hasSize(3);
            
            // Verify images are properly associated
            for (Image image : raffleImages) {
                assertThat(image.getRaffle()).isEqualTo(testRaffle);
                assertThat(image.getAssociation()).isEqualTo(authData.association());
                assertThat(image.getUrl()).contains("/raffles/" + testRaffle.getId() + "/images/");
            }

            // Verify no pending images remain
            List<Image> remainingPendingImages = imagesRepository.findAllByRaffleIsNullAndAssociation(authData.association());
            assertThat(remainingPendingImages).isEmpty();
        }

        @Test
        @DisplayName("Should successfully edit raffle images by replacing existing ones")
        void shouldEditRaffleImagesByReplacingExistingOnes() throws Exception {
            // Arrange - Add some images to the raffle first
            List<Image> existingImages = createImagesForRaffle(testRaffle, 2);
            testRaffle.getImages().addAll(existingImages);
            rafflesRepository.save(testRaffle);

            // Create new pending images to replace the existing ones
            List<Image> newPendingImages = createPendingImagesForAssociation(2);
            List<ImageDTO> newImageDTOs = convertToImageDTOs(newPendingImages);

            RaffleEdit raffleEdit = new RaffleEdit(
                    null,
                    null,
                    null,
                    newImageDTOs,
                    null,
                    null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.images", hasSize(2)));

            // Verify that the new images are associated
            List<Image> raffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(raffleImages).hasSize(2);
            
            // Verify these are the new images (not the old ones)
            List<Long> newImageIds = newPendingImages.stream().map(Image::getId).toList();
            List<Long> raffleImageIds = raffleImages.stream().map(Image::getId).toList();
            assertThat(raffleImageIds).containsExactlyInAnyOrderElementsOf(newImageIds);
        }

        @Test
        @DisplayName("Should handle gracefully when some requested images are missing during edit")
        void shouldHandleGracefullyWhenSomeRequestedImagesAreMissingDuringEdit() throws Exception {
            // Arrange - Create 2 pending images but reference 3 in the request (1 missing)
            List<Image> pendingImages = createPendingImagesForAssociation(2);
            
            List<ImageDTO> imageDTOs = new ArrayList<>();
            // Add existing images
            imageDTOs.addAll(convertToImageDTOs(pendingImages));
            // Add non-existent image
            imageDTOs.add(new ImageDTO(99999L, "non-existent.jpg", "/path", "image/jpeg", "http://example.com", 3));

            RaffleEdit raffleEdit = new RaffleEdit(
                    null,
                    null,
                    null,
                    imageDTOs, // Mixed existing and missing images
                    null,
                    null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert - Should succeed with only the existing images (2 valid images remain)
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.images", hasSize(2))); // Only 2 existing images

            // Verify database state - only existing images were associated
            List<Image> raffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(raffleImages).hasSize(2); // Only existing images were associated
        }

        @Test
        @DisplayName("Should return 400 when trying to edit raffle to have no images")
        void shouldReturn400WhenTryingToEditRaffleToHaveNoImages() throws Exception {
            // Arrange - Add some images to the raffle first
            List<Image> existingImages = createImagesForRaffle(testRaffle, 3);
            testRaffle.getImages().addAll(existingImages);
            rafflesRepository.save(testRaffle);

            RaffleEdit raffleEdit = new RaffleEdit(
                    null,
                    null,
                    null,
                    List.of(), // Empty list to remove all images
                    null,
                    null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert - Should fail because raffles must have at least one image
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.images").value("INVALID_LENGTH"));

            // Verify raffle still has its original images
            List<Image> raffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(raffleImages).hasSize(3); // Original images still there
        }

        @Test
        @DisplayName("Should return 400 when all requested images in edit are missing")
        void shouldReturn400WhenAllRequestedImagesInEditAreMissing() throws Exception {
            // Arrange - Add some images to the raffle first
            List<Image> existingImages = createImagesForRaffle(testRaffle, 2);
            testRaffle.getImages().addAll(existingImages);
            rafflesRepository.save(testRaffle);

            // Request only non-existent images
            List<ImageDTO> imageDTOs = List.of(
                    new ImageDTO(99998L, "missing1.jpg", "/path", "image/jpeg", "http://example.com", 1),
                    new ImageDTO(99999L, "missing2.jpg", "/path", "image/jpeg", "http://example.com", 2)
            );

            RaffleEdit raffleEdit = new RaffleEdit(
                    null,
                    null,
                    null,
                    imageDTOs, // All missing images
                    null,
                    null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert - Should fail because raffles must have at least one image
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("A raffle must have at least one image."));

            // Verify raffle still has its original images
            List<Image> raffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(raffleImages).hasSize(2); // Original images still there
        }

        @Test
        @DisplayName("Should succeed when editing with some missing images but at least one valid image remains")
        void shouldSucceedWhenEditingWithSomeMissingImagesButValidImagesRemain() throws Exception {
            // Arrange - Create 1 pending image and reference 2 in edit request (1 valid, 1 missing)
            List<Image> pendingImages = createPendingImagesForAssociation(1);
            
            List<ImageDTO> imageDTOs = new ArrayList<>();
            // Add existing image
            imageDTOs.addAll(convertToImageDTOs(pendingImages));
            // Add non-existent image
            imageDTOs.add(new ImageDTO(99999L, "non-existent.jpg", "/path", "image/jpeg", "http://example.com", 2));

            RaffleEdit raffleEdit = new RaffleEdit(
                    null,
                    null,
                    null,
                    imageDTOs,
                    null,
                    null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert - Should succeed because there's at least one valid image
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.images", hasSize(1))); // Only 1 valid image

            // Verify database state - only the valid image was associated
            List<Image> raffleImages = imagesRepository.findAllByRaffle(testRaffle);
            assertThat(raffleImages).hasSize(1); // Only the valid image was associated
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Arrange
            RaffleEdit raffleEdit = new RaffleEdit(
                    "Updated Title", null, null, null, null, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit)));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when user doesn't belong to association")
        void shouldReturn403WhenUserDoesntBelongToAssociation() throws Exception {
            // Arrange
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserWithCredentials(
                    "otheruser", "other@example.com", "password123");
            
            RaffleEdit raffleEdit = new RaffleEdit(
                    "Updated Title", null, null, null, null, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(otherUserData.user().getEmail())));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when raffle doesn't exist")
        void shouldReturn404WhenRaffleDoesntExist() throws Exception {
            // Arrange
            String nonExistentRaffleEndpoint = "/v1/associations/" + authData.association().getId() + "/raffles/99999";
            RaffleEdit raffleEdit = new RaffleEdit(
                    "Updated Title", null, null, null, null, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(nonExistentRaffleEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when trying to use images from different association")
        void shouldReturn400WhenUsingImagesFromDifferentAssociation() throws Exception {
            // Arrange
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserWithCredentials(
                    "otheruser2", "other2@example.com", "password123");
            
            // Create image belonging to different association
            Image otherAssociationImage = TestDataBuilder.image()
                    .association(otherUserData.association())
                    .pendingImage()
                    .build();
            otherAssociationImage = imagesRepository.save(otherAssociationImage);

            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, null,
                    List.of(new ImageDTO(otherAssociationImage.getId(),
                            otherAssociationImage.getFileName(),
                            otherAssociationImage.getFilePath(),
                            otherAssociationImage.getContentType(),
                            otherAssociationImage.getUrl(),
                            1)),
                    null, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should successfully edit ticket price")
        void shouldEditTicketPrice() throws Exception {
            // Arrange
            BigDecimal newPrice = BigDecimal.valueOf(25.75);
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, null, null, newPrice, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.ticketPrice").value(25.75));

            // Verify database state
            Raffle updatedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(updatedRaffle.getTicketPrice()).isEqualByComparingTo(newPrice);
        }

        @Test
        @DisplayName("Should successfully edit total tickets by increasing count")
        void shouldEditTotalTicketsByIncreasingCount() throws Exception {
            // Arrange - Setup raffle with initial tickets
            setupRaffleWithTickets(testRaffle, 10L, 1L);
            Long newTotal = 20L;
            
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, null, null, null, newTotal
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalTickets").value(20));

            // Verify database state
            Raffle updatedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(updatedRaffle.getTotalTickets()).isEqualTo(20L);
            
            // Verify additional tickets were created
            List<Ticket> allTickets = ticketsRepository.findAllByRaffle(updatedRaffle);
            assertThat(allTickets).hasSize(20);
            
            // Verify statistics were updated
            assertThat(updatedRaffle.getStatistics().getAvailableTickets()).isEqualTo(20L);
        }

        @Test
        @DisplayName("Should successfully edit total tickets by keeping same count")
        void shouldEditTotalTicketsByKeepingSameCount() throws Exception {
            // Arrange - Setup raffle with initial tickets
            setupRaffleWithTickets(testRaffle, 10L, 1L);
            Long sameTotal = 10L;
            
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, null, null, null, sameTotal
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalTickets").value(10));

            // Verify no additional tickets were created
            List<Ticket> allTickets = ticketsRepository.findAllByRaffle(testRaffle);
            assertThat(allTickets).hasSize(10);
        }

        @Test
        @DisplayName("Should return 400 when trying to decrease total tickets below sold count")
        void shouldReturn400WhenDecreasingTotalTicketsBelowSoldCount() throws Exception {
            // Arrange - Setup raffle with tickets, some sold
            setupRaffleWithTickets(testRaffle, 10L, 1L);
            sellTickets(testRaffle, 5); // Sell 5 tickets
            Long newTotal = 3L; // Try to set total below sold count
            
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, null, null, null, newTotal
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The total tickets count cannot be less than the number of tickets already sold for this raffle"));

            // Verify raffle remains unchanged
            Raffle unchangedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(unchangedRaffle.getTotalTickets()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should successfully edit end date")
        void shouldEditEndDate() throws Exception {
            // Arrange
            LocalDateTime newEndDate = LocalDateTime.now().plusDays(14);
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, newEndDate, null, null, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Verify database state
            Raffle updatedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(updatedRaffle.getEndDate()).isEqualToIgnoringNanos(newEndDate);
        }

        @Test
        @DisplayName("Should reactivate completed raffle when end date is extended")
        void shouldReactivateCompletedRaffleWhenEndDateExtended() throws Exception {
            // Arrange - Make raffle completed due to end date reached
            testRaffle.setStatus(RaffleStatus.COMPLETED);
            testRaffle.setCompletionReason(CompletionReason.END_DATE_REACHED);
            testRaffle.setCompletedAt(LocalDateTime.now().minusDays(1));
            rafflesRepository.save(testRaffle);

            LocalDateTime newEndDate = LocalDateTime.now().plusDays(7);
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, newEndDate, null, null, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            // Verify raffle was reactivated
            Raffle updatedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(updatedRaffle.getStatus()).isEqualTo(RaffleStatus.ACTIVE);
            assertThat(updatedRaffle.getCompletionReason()).isNull();
            assertThat(updatedRaffle.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("Should reactivate completed raffle when total tickets increased and was completed due to all tickets sold")
        void shouldReactivateRaffleWhenTicketsIncreasedAndWasCompletedDueToAllTicketsSold() throws Exception {
            // Arrange - Setup raffle completed due to all tickets sold
            setupRaffleWithTickets(testRaffle, 5L, 1L);
            sellTickets(testRaffle, 5); // Sell all tickets
            testRaffle.setStatus(RaffleStatus.COMPLETED);
            testRaffle.setCompletionReason(CompletionReason.ALL_TICKETS_SOLD);
            testRaffle.setCompletedAt(LocalDateTime.now().minusHours(1));
            rafflesRepository.save(testRaffle);

            Long newTotal = 10L; // Increase total tickets
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, null, null, null, newTotal
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            // Verify raffle was reactivated and tickets created
            Raffle updatedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(updatedRaffle.getStatus()).isEqualTo(RaffleStatus.ACTIVE);
            assertThat(updatedRaffle.getCompletionReason()).isNull();
            assertThat(updatedRaffle.getCompletedAt()).isNull();
            assertThat(updatedRaffle.getTotalTickets()).isEqualTo(10L);
            
            // Verify additional tickets were created
            List<Ticket> allTickets = ticketsRepository.findAllByRaffle(updatedRaffle);
            assertThat(allTickets).hasSize(10);
            
            // Verify statistics
            assertThat(updatedRaffle.getStatistics().getAvailableTickets()).isEqualTo(5L); // 5 new available
            assertThat(updatedRaffle.getStatistics().getSoldTickets()).isEqualTo(5L); // 5 still sold
        }

        @Test
        @DisplayName("Should successfully perform combined edit of multiple fields")
        void shouldPerformCombinedEditOfMultipleFields() throws Exception {
            // Arrange
            List<Image> pendingImages = createPendingImagesForAssociation(2);
            setupRaffleWithTickets(testRaffle, 10L, 1L);
            
            RaffleEdit raffleEdit = new RaffleEdit(
                    "New Title",
                    "New Description", 
                    LocalDateTime.now().plusDays(14),
                    convertToImageDTOs(pendingImages),
                    BigDecimal.valueOf(20.00), // New price
                    15L // Increase total tickets
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("New Title"))
                    .andExpect(jsonPath("$.data.description").value("New Description"))
                    .andExpect(jsonPath("$.data.totalTickets").value(15))
                    .andExpect(jsonPath("$.data.ticketPrice").value(20.00))
                    .andExpect(jsonPath("$.data.images", hasSize(2)));

            // Verify database state
            Raffle updatedRaffle = rafflesRepository.findById(testRaffle.getId()).orElseThrow();
            assertThat(updatedRaffle.getTitle()).isEqualTo("New Title");
            assertThat(updatedRaffle.getDescription()).isEqualTo("New Description");
            assertThat(updatedRaffle.getTotalTickets()).isEqualTo(15L);
            assertThat(updatedRaffle.getTicketPrice()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
            
            // Verify images were associated
            List<Image> raffleImages = imagesRepository.findAllByRaffle(updatedRaffle);
            assertThat(raffleImages).hasSize(2);
            
            // Verify additional tickets were created
            List<Ticket> allTickets = ticketsRepository.findAllByRaffle(updatedRaffle);
            assertThat(allTickets).hasSize(15);
        }

        @Test
        @DisplayName("Should return 400 when end date is in the past")
        void shouldReturn400WhenEndDateIsInPast() throws Exception {
            // Arrange
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
            RaffleEdit raffleEdit = new RaffleEdit(
                    null, null, pastDate, null, null, null
            );

            // Act
            ResultActions result = mockMvc.perform(put(baseEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(raffleEdit))
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // Helper Methods

    private List<Image> createPendingImagesForAssociation(int count) {
        List<Image> images = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Image image = TestDataBuilder.image()
                    .fileName("pending-image-" + (i + 1) + ".jpg")
                    .association(authData.association())
                    .pendingImage()
                    .imageOrder(i + 1)
                    .build();
            images.add(imagesRepository.save(image));
        }
        return images;
    }

    private List<Image> createImagesForRaffle(Raffle raffle, int count) {
        List<Image> images = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Image image = TestDataBuilder.image()
                    .fileName("raffle-image-" + (i + 1) + ".jpg")
                    .association(authData.association())
                    .raffle(raffle)
                    .imageOrder(i + 1)
                    .build();
            images.add(imagesRepository.save(image));
        }
        return images;
    }

    private List<ImageDTO> convertToImageDTOs(List<Image> images) {
        return images.stream()
                .map(image -> new ImageDTO(
                        image.getId(),
                        image.getFileName(),
                        image.getFilePath(),
                        image.getContentType(),
                        image.getUrl(),
                        image.getImageOrder()))
                .toList();
    }

    private void setupRaffleWithTickets(Raffle raffle, Long totalTickets, Long firstTicketNumber) {
        // Set raffle properties
        raffle.setTotalTickets(totalTickets);
        raffle.setFirstTicketNumber(firstTicketNumber);
        
        // Initialize statistics if not present
        if (raffle.getStatistics() == null) {
            raffle.setStatistics(RaffleStatistics.builder()
                    .availableTickets(totalTickets)
                    .soldTickets(0L)
                    .revenue(BigDecimal.ZERO)
                    .totalOrders(0L)
                    .participants(0L)
                    .build());
        } else {
            raffle.getStatistics().setAvailableTickets(totalTickets);
            raffle.getStatistics().setSoldTickets(0L);
        }
        
        rafflesRepository.save(raffle);
        
        // Create tickets
        for (long i = 0; i < totalTickets; i++) {
            Ticket ticket = Ticket.builder()
                    .raffle(raffle)
                    .ticketNumber(String.valueOf(firstTicketNumber + i))
                    .status(TicketStatus.AVAILABLE)
                    .build();
            ticketsRepository.save(ticket);
        }
    }

    private void sellTickets(Raffle raffle, int count) {
        List<Ticket> availableTickets = ticketsRepository.findByRaffleAndStatus(raffle, TicketStatus.AVAILABLE);
        
        for (int i = 0; i < count && i < availableTickets.size(); i++) {
            Ticket ticket = availableTickets.get(i);
            ticket.setStatus(TicketStatus.SOLD);
            ticketsRepository.save(ticket);
        }
        
        // Update statistics
        raffle.getStatistics().setSoldTickets((long) count);
        raffle.getStatistics().setAvailableTickets(raffle.getTotalTickets() - count);
        rafflesRepository.save(raffle);
    }
} 