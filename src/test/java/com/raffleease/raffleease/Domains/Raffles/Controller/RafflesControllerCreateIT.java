package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Images.Repository.ImagesRepository;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.Domains.Tickets.DTO.TicketsCreate;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Domains.Tickets.Repository.TicketsRepository;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import com.raffleease.raffleease.Helpers.AssociationRegisterBuilder;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import com.raffleease.raffleease.Helpers.TicketsCreateBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.PENDING;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RafflesControllerCreateIT extends BaseRafflesIT {
    @Autowired
    private RafflesRepository rafflesRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private TicketsRepository ticketsRepository;

    @Autowired
    private TokensQueryService tokensQueryService;

    @Autowired
    private AssociationsService associationsService;

    @Value("${spring.storage.images.base_path}")
    private String basePath;

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
        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).endsWith("/api/v1/raffles/" + createdRaffleId);

        // 6. Verify raffle in database
        Raffle raffle = rafflesRepository.findById(createdRaffleId).orElseThrow();
        assertThat(raffle.getTitle()).isEqualTo(raffleCreate.title());
        assertThat(raffle.getDescription()).isEqualTo(raffleCreate.description());
        assertThat(raffle.getStatus()).isEqualTo(PENDING);

        String subject = tokensQueryService.getSubject(accessToken);
        Association association = associationsService.findById(Long.parseLong(subject));
        assertThat(raffle.getAssociation()).isNotNull();
        assertThat(raffle.getAssociation().equals(association));

        // 7. Verify images
        List<Image> storedImages = imagesRepository.findAllByRaffle(raffle);
        assertThat(storedImages.size()).isEqualTo(2);
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
        assertThat(ticketsInDb.size()).isEqualTo(5);

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
        AssociationRegister otherUser = new AssociationRegisterBuilder()
                .withEmail("otheruser@example.com")
                .withName("Another Association")
                .withPhoneNumber("998877665")
                .build();
        String token = performAuthentication(otherUser);

        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1, token).andReturn());

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(images)
                .build();

        // Try to use those images from the first user
        performCreateRaffleRequest(raffle)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to use the specified image(s)"));
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

        ImageDTO image1 = withCustomOrder(uploaded.get(0), 1);
        ImageDTO image2 = withCustomOrder(uploaded.get(1), 1);

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

        ImageDTO image1 = withCustomOrder(uploaded.get(0), 1);
        ImageDTO image2 = withCustomOrder(uploaded.get(1), 3);

        RaffleCreate raffle = new RaffleCreateBuilder()
                .withImages(List.of(image1, image2))
                .build();

        performCreateRaffleRequest(raffle)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Image orders must be consecutive starting from 1"));
    }

    private ImageDTO withCustomOrder(ImageDTO original, int order) {
        return ImageDTO.builder()
                .id(original.id())
                .fileName(original.fileName())
                .filePath(original.filePath())
                .contentType(original.contentType())
                .url(original.url())
                .imageOrder(order)
                .build();
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