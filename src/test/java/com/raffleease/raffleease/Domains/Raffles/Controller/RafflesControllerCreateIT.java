package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import com.raffleease.raffleease.Helpers.TestUtils;
import com.raffleease.raffleease.Helpers.TicketsCreateBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.PENDING;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.net.HttpHeaders.LOCATION;

class RafflesControllerCreateIT extends BaseRafflesIT {
    @Value("${spring.storage.images.base_path}")
    private String basePath;

    @Value("${spring.application.host.client}")
    private String clientHost;

    @Test
    void shouldCreateRaffle() throws Exception {
        // 1. Upload mock images
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());

        // 2. Create raffle using builder
        RaffleCreate raffleCreate = new RaffleCreateBuilder()
                .withImages(images)
                .build();

        // 3. Perform raffle creation request
        MvcResult result = performCreateRaffleRequest(raffleCreate)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New raffle created successfully"))
                .andExpect(jsonPath("$.data.title").value(raffleCreate.title()))
                .andExpect(jsonPath("$.data.description").value(raffleCreate.description()))
                .andExpect(jsonPath("$.data.status").value(PENDING.toString()))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Long createdRaffleId = json.path("data").path("id").asLong();

        // 5. Validate URI
        String locationHeader = result.getResponse().getHeader(LOCATION);
        assertThat(locationHeader).endsWith("/api/v1/associations/" + associationId + "/raffles/" + createdRaffleId);

        // 6. Verify raffle in database
        Raffle raffle = rafflesRepository.findById(createdRaffleId).orElseThrow();
        assertThat(raffle).isNotNull();
        assertThat(raffle.getTitle()).isEqualTo(raffleCreate.title());
        assertThat(raffle.getDescription()).isEqualTo(raffleCreate.description());
        assertThat(raffle.getStatus()).isEqualTo(PENDING);
        assertThat(raffle.getURL()).isEqualTo(clientHost + "/client/raffle/" + raffle.getId());
        assertThat(ChronoUnit.MILLIS.between(raffle.getEndDate(), raffleCreate.endDate())).isLessThanOrEqualTo(1);
        assertThat(raffle.getTicketPrice()).isEqualTo(raffleCreate.ticketsInfo().price());
        assertThat(raffle.getFirstTicketNumber()).isEqualTo(raffleCreate.ticketsInfo().lowerLimit());
        assertThat(raffle.getTotalTickets()).isEqualTo(raffleCreate.ticketsInfo().amount());
        assertThat(raffle.getAvailableTickets()).isEqualTo(raffleCreate.ticketsInfo().amount());
        assertThat(raffle.getSoldTickets()).isEqualTo(0L);
        assertThat(raffle.getRevenue()).isEqualTo(ZERO.setScale(2));
        assertThat(raffle.getCreatedAt()).isNotNull();

        String subject = tokensQueryService.getSubject(accessToken);
        Association association = associationsRepository.findById(Long.parseLong(subject)).orElseThrow();
        assertThat(raffle.getAssociation()).isNotNull();
        assertThat(raffle.getAssociation().equals(association));

        // 7. Verify images
        List<Image> storedImages = imagesRepository.findAllByRaffle(raffle);
        assertThat(storedImages.size()).isEqualTo(images.size());
        for (Image img : storedImages) {
            assertThat(img.getRaffle().getId()).isEqualTo(raffle.getId());

            assertThat(imagesRepository.findById(img.getId())).isNotNull();
            assertThat(img.getUrl()).contains("/api/v1/raffles/" + raffle.getId() + "/images/" + img.getId());

            Path expectedPath = Paths.get(basePath, "associations", raffle.getAssociation().getId().toString(), "raffles", raffle.getId().toString(), "images");
            assertThat(img.getFilePath()).startsWith(expectedPath.toString());
            assertThat(Files.exists(Paths.get(img.getFilePath()))).isTrue();
        }

        // 8. Verify tickets
        List<Ticket> ticketsInDb = ticketsRepository.findAllByRaffle(raffle);
        assertThat((long) ticketsInDb.size()).isEqualTo(raffleCreate.ticketsInfo().amount());

        long expectedTicketNumber = raffleCreate.ticketsInfo().lowerLimit();
        for (Ticket ticket : ticketsInDb) {
            assertThat(ticket.getTicketNumber()).isEqualTo(String.valueOf(expectedTicketNumber++));
            assertThat(ticket.getStatus()).isEqualTo(AVAILABLE);
            assertThat(ticket.getRaffle().getId()).isEqualTo(raffle.getId());
        }
    }

    @Test
    void shouldFailWhenTitleIsNull() throws Exception {
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withTitle(null)
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Raffle title is required"));
    }

    @Test
    void shouldFailWhenTitleExceedsMaxLength() throws Exception {
        String longTitle = "A".repeat(101);

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withTitle(longTitle)
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Tile cannot exceed 100 characters"));
    }

    @Test
    void shouldFailWhenDescriptionIsBlank() throws Exception {
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withDescription(" ")
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").value("Raffle's description is required"));
    }

    @Test
    void shouldFailWhenDescriptionExceedsMaxLength() throws Exception {
        String longDescription = "A".repeat(5001);

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withDescription(longDescription)
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").value("Description cannot exceed 5000 characters"));
    }

    @Test
    void shouldFailWhenEndDateIsNull() throws Exception {
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withEndDate(null)
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endDate").value("Raffle's end date is required"));
    }

    @Test
    void shouldFailWhenEndDateIsInPast() throws Exception {
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withEndDate(LocalDateTime.now().minusDays(1))
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endDate").value("Raffle's end date must be in the future"));
    }

    @Test
    void shouldFailWhenImagesListIsNull() throws Exception {
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(null)
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.images").value("Must provide at least one picture for raffle"));
    }

    @Test
    void shouldFailWhenImagesListIsEmpty() throws Exception {
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(List.of())
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.images").value("A minimum of 1 and a maximum of 10 images are allowed"));
    }

    @Test
    void shouldFailWhenImageIdsDoNotExist() throws Exception {
        long nonExistentImageId = 99999L;

        ImageDTO fakeImage = ImageDTO.builder()
                .id(nonExistentImageId)
                .fileName("fake.jpg")
                .filePath("some/path/fake.jpg")
                .contentType("image/jpeg")
                .url("http://localhost/api/v1/images/" + nonExistentImageId)
                .imageOrder(1)
                .build();

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(List.of(fakeImage))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("One or more images were not found"));
    }

    @Test
    void shouldFailWhenUsingImagesFromAnotherAssociation() throws Exception {
        // Register second association
        AuthResponse authResponse = registerOtherUser();
        String otherToken = authResponse.accessToken();
        Long associationId = authResponse.association().id();

        // Upload images for second association raffle
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1, otherToken, associationId).andReturn());

        assertThat(images).isNotNull();

        // Try to create new raffle for original association using these images
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(images)
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to use the specified image(s)"));
    }

    @Test
    void shouldFailIfAdminDoesNotBelongToAssociation() throws Exception {
        String otherToken = registerOtherUser().accessToken();

        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        RaffleCreate raffleCreate = new RaffleCreateBuilder().withImages(images).build();

        performCreateRaffleRequest(raffleCreate, otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a member of this association"));
    }

    @Test
    void shouldFailWhenDuplicateImageIdsAreProvided() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        ImageDTO img = images.get(0);

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(List.of(img, img))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate image IDs found in request"));
    }

    @Test
    void shouldFailWhenImageOrdersAreDuplicated() throws Exception {
        List<ImageDTO> uploaded = parseImagesFromResponse(uploadImages(2).andReturn());

        ImageDTO image1 = copyWithNewOrder(uploaded.get(0), 1);
        ImageDTO image2 = copyWithNewOrder(uploaded.get(1), 1);

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(List.of(image1, image2))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate image orders detected"));
    }

    @Test
    void shouldFailWhenImageOrdersAreNotConsecutive() throws Exception {
        List<ImageDTO> uploaded = parseImagesFromResponse(uploadImages(2).andReturn());

        ImageDTO image1 = copyWithNewOrder(uploaded.get(0), 1);
        ImageDTO image2 = copyWithNewOrder(uploaded.get(1), 3);

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(List.of(image1, image2))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Image orders must be consecutive starting from 1"));
    }

    @Test
    void shouldCreateRaffleExcludingDeletedImage() throws Exception {
        // 1. Upload 3 images
        List<ImageDTO> uploaded = parseImagesFromResponse(uploadImages(3).andReturn());

        // 2. Delete second image
        ImageDTO toDelete = uploaded.get(1);
        Long imageIdToDelete = toDelete.id();
        performImageDelete("/api/v1/associations/"+ associationId + "/images/" + imageIdToDelete);

        // 3. Check that the deleted image is not in DB
        Optional<Image> deleted = imagesRepository.findById(imageIdToDelete);
        assertThat(deleted).isEmpty();

        // 4. Check that the file is removed from the filesystem
        Path deletedPath = Paths.get(toDelete.filePath());
        assertThat(Files.exists(deletedPath)).isFalse();

        // 5. Reorder remaining images
        List<ImageDTO> reordered = new ArrayList<>();
        int order = 1;
        for (ImageDTO image : uploaded) {
            if (!image.id().equals(imageIdToDelete)) {
                reordered.add(copyWithNewOrder(image, order++));
            }
        }

        // 6. Create the raffle with the remaining images
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(reordered)
                .build();

        MvcResult result = performCreateRaffleRequest(raffle)
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Long raffleId = json.path("data").path("id").asLong();
        Raffle saved = rafflesRepository.findById(raffleId).orElseThrow();

        // 7. Check that the deleted image is not associated to the raffle
        List<Image> raffleImages = imagesRepository.findAllByRaffle(saved);
        assertThat(raffleImages.stream().noneMatch(img -> img.getId().equals(imageIdToDelete))).isTrue();

        // 8. Check that the stored images match the reordered list
        assertThat(raffleImages.size()).isEqualTo(2);
        for (int i = 0; i < raffleImages.size(); i++) {
            assertThat(raffleImages.get(i).getImageOrder()).isEqualTo(i + 1);
            assertThat(raffleImages.get(i).getRaffle().getId()).isEqualTo(raffleId);
            assertThat(Files.exists(Paths.get(raffleImages.get(i).getFilePath()))).isTrue();
        }
    }

    @Test
    void shouldFailWhenTicketsInfoIsNull() throws Exception {
        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .withTicketsInfo(null)
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketsInfo").value("Raffle ticket's info is required"));
    }

    @Test
    void shouldFailWhenTicketAmountIsInvalid() throws Exception {
        TicketsCreate invalid = new TicketsCreateBuilder().withAmount(0L).build();

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .withTicketsInfo(invalid)
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['ticketsInfo.amount']").value("Tickets amount must be greater than zero"));
    }

    @Test
    void shouldFailWhenTicketPriceIsInvalid() throws Exception {
        TicketsCreate invalid = new TicketsCreateBuilder().withPrice(new BigDecimal("0.00")).build();

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .withTicketsInfo(invalid)
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['ticketsInfo.price']").value("Price must be greater than zero"));
    }

    @Test
    void shouldFailWhenTicketLowerLimitIsInvalid() throws Exception {
        TicketsCreate invalid = new TicketsCreateBuilder().withLowerLimit(-1L).build();

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(parseImagesFromResponse(uploadImages(2).andReturn()))
                .withTicketsInfo(invalid)
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['ticketsInfo.lowerLimit']").value("Lower limit must be greater than or equal to zero"));
    }
}