package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.raffleease.raffleease.Domains.Auth.DTOs.AuthResponse;
import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleEdit;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Tickets.Model.Ticket;
import com.raffleease.raffleease.Helpers.RaffleCreateBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.AVAILABLE;
import static com.raffleease.raffleease.Domains.Tickets.Model.TicketStatus.SOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RafflesControllerUpdateIT extends BaseRafflesIT {
    @Value("${spring.storage.images.base_path}")
    private String basePath;
    private Long raffleId;
    private List<ImageDTO> originalImages;

    @BeforeEach
    void setUp() throws Exception {
        originalImages = parseImagesFromResponse(uploadImages(2).andReturn());
        RaffleCreate raffleCreate = new RaffleCreateBuilder().withImages(originalImages).build();
        raffleId = parseRaffleId(performCreateRaffleRequest(raffleCreate, associationId, accessToken).andReturn());
    }

    @Test
    void shouldEditRaffleSuccessfully() throws Exception {
        // 1. Upload new images
        MvcResult uploadResult = uploadImagesForRaffle(2, raffleId).andReturn();
        List<ImageDTO> newImages = parseImagesFromResponse(uploadResult);
        List<ImageDTO> allImages = Stream.concat(originalImages.stream(), newImages.stream()).toList();

        // 2. Build edit request
        RaffleEdit editRequest = RaffleEdit.builder()
                .title("Updated Title")
                .description("Updated description")
                .endDate(LocalDateTime.now().plusDays(10))
                .images(allImages)
                .ticketPrice(new BigDecimal("2.50"))
                .totalTickets(15L)
                .price(new BigDecimal("2.50"))
                .build();

        // 3. Perform edit request
        performEditRaffleRequest(raffleId, editRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.data.images", hasSize(4)))
                .andExpect(jsonPath("$.data.totalTickets").value(15))
                .andExpect(jsonPath("$.data.ticketPrice").value("2.5"));

        // 4. Validate images updated in DB
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        List<Image> updatedImages = imagesRepository.findAllByRaffle(raffle);
        assertThat(updatedImages.size()).isEqualTo(originalImages.size() + newImages.size());

        for (Image img : updatedImages) {
            assertThat(img.getRaffle().getId()).isEqualTo(raffle.getId());

            assertThat(imagesRepository.findById(img.getId())).isNotNull();
            assertThat(img.getUrl()).contains("/api/v1/associations/" + associationId + "/raffles/" + raffle.getId() + "/images/" + img.getId());

            Path expectedPath = Paths.get(basePath, "associations", raffle.getAssociation().getId().toString(), "raffles", raffle.getId().toString(), "images");
            assertThat(img.getFilePath()).startsWith(expectedPath.toString());
            assertThat(Files.exists(expectedPath)).isTrue();
        }

        List<Ticket> ticketsInDb = ticketsRepository.findAllByRaffle(raffle);
        assertThat(ticketsInDb.size()).isEqualTo(15);

        long expectedTicketNumber = raffle.getFirstTicketNumber();
        for (Ticket ticket : ticketsInDb) {
            assertThat(ticket.getTicketNumber()).isEqualTo(String.valueOf(expectedTicketNumber++));
            assertThat(ticket.getStatus()).isEqualTo(AVAILABLE);
            assertThat(ticket.getRaffle().getId()).isEqualTo(raffle.getId());
        }
    }

    @Test
    void shouldUpdateImagesOrderOnRaffleEdit() throws Exception {
        ImageDTO image1 = copyWithNewOrder(originalImages.get(0), 2);
        ImageDTO image2 = copyWithNewOrder(originalImages.get(1), 1);
        List<ImageDTO> requestImages = List.of(image1, image2);

        RaffleEdit request = RaffleEdit.builder()
                .images(requestImages)
                .build();
        performEditRaffleRequest(raffleId, request)
                .andExpect(status().isOk())
                .andReturn();

        for (ImageDTO requestImage : requestImages) {
            Image savedImage = imagesRepository.findById(requestImage.id()).orElseThrow();
            assertThat(savedImage.getImageOrder()).isEqualTo(requestImage.imageOrder());
        }
    }

    @Test
    void shouldAddNewImageAndUpdateOrder() throws Exception {
        ImageDTO newImage = parseImagesFromResponse(uploadImages(1).andReturn()).get(0);
        ImageDTO image1 = copyWithNewOrder(originalImages.get(0), 3);
        ImageDTO image2 = copyWithNewOrder(originalImages.get(1), 2);
        ImageDTO image3 = copyWithNewOrder(newImage, 1);

        List<ImageDTO> requestImages = List.of(image1, image2, image3);

        RaffleEdit request = RaffleEdit.builder()
                .images(requestImages)
                .build();
        performEditRaffleRequest(raffleId, request)
                .andExpect(status().isOk())
                .andReturn();

        for (ImageDTO requestImage : requestImages) {
            Image savedImage = imagesRepository.findById(requestImage.id()).orElseThrow();
            assertThat(savedImage.getImageOrder()).isEqualTo(requestImage.imageOrder());
        }
    }

    @Test
    void shouldFailWhenTitleExceedsMaxLength() throws Exception {
        RaffleEdit edit = RaffleEdit.builder()
                .title("A".repeat(101))
                .images(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("INVALID_LENGTH"));
    }

    @Test
    void shouldFailWhenDescriptionExceedsMaxLength() throws Exception {
        RaffleEdit edit = RaffleEdit.builder()
                .description("A".repeat(5001))
                .images(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").value("INVALID_LENGTH"));
    }

    @Test
    void shouldFailWhenEndDateIsInPast() throws Exception {
        RaffleEdit edit = RaffleEdit.builder()
                .endDate(LocalDateTime.now().minusDays(1))
                .images(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endDate").value("MUST_BE_IN_FUTURE"));
    }

    @Test
    void shouldFailWhenImagesListIsEmpty() throws Exception {
        RaffleEdit edit = RaffleEdit.builder().images(List.of()).build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.images").value("INVALID_LENGTH"));
    }

    @Test
    void shouldFailWhenImagesListIsTooShort() throws Exception {
        RaffleEdit editWithZero = RaffleEdit.builder()
                .images(List.of())
                .build();

        performEditRaffleRequest(raffleId, editWithZero)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.images").value("INVALID_LENGTH"));
    }

    @Test
    void shouldFailWhenImageIdsAreDuplicatedOnEdit() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        ImageDTO image = images.get(0);

        RaffleEdit edit = RaffleEdit.builder()
                .images(List.of(image, image))
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate image IDs found in request"));
    }


    @Test
    void shouldFailWhenImageOrdersAreDuplicated() throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(2).andReturn());
        ImageDTO image1 = copyWithNewOrder(images.get(0), 1);
        ImageDTO image2 = copyWithNewOrder(images.get(1), 1);

        RaffleEdit edit = RaffleEdit.builder().images(List.of(image1, image2)).build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate image orders detected"));
    }

    @Test
    void shouldFailWhenImageIdsDoNotExist() throws Exception {
        long nonExistentImageId = 99999L;

        ImageDTO fakeImage = ImageDTO.builder()
                .id(nonExistentImageId)
                .fileName("fake.jpg")
                .filePath("some/path/fake.jpg")
                .contentType("image/jpeg")
                .url("http://localhost/api/v1/associations/" + associationId + "/images/" + nonExistentImageId)
                .imageOrder(1)
                .build();

        RaffleEdit edit = RaffleEdit.builder()
                .images(List.of(fakeImage))
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("One or more images were not found"));
    }

    @Test
    void shouldFailWhenTicketPriceIsZeroOrNegativeOnEdit() throws Exception {
        RaffleEdit edit = RaffleEdit.builder()
                .ticketPrice(new BigDecimal("0.00"))
                .images(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketPrice").value("TOO_SMALL"));
    }

    @Test
    void shouldFailWhenImageBelongsToAnotherAssociation() throws Exception {
        // Create image with different user
        AuthResponse authResponse = registerOtherUser();
        String otherToken = authResponse.accessToken();
        Long associationId = authResponse.associationId();
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1, otherToken, associationId).andReturn());

        RaffleEdit edit = RaffleEdit.builder()
                .images(images)
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to use the specified image(s)"));
    }

    @Test
    void shouldFailWhenImageBelongsToDifferentRaffle() throws Exception {
        List<ImageDTO> otherImages = parseImagesFromResponse(uploadImages(2).andReturn());
        RaffleCreate raffleCreate = new RaffleCreateBuilder().withImages(otherImages).build();
        performCreateRaffleRequest(raffleCreate, associationId, accessToken);
        RaffleEdit editRequest = RaffleEdit.builder().images(otherImages).build();
        performEditRaffleRequest(raffleId, editRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("One or more images are already associated with a different raffle"));
    }

    @Test
    @Transactional
    void shouldDeleteImageReorderAndUpdateRaffleImages() throws Exception {
        // 1. Delete first image
        ImageDTO toDelete = originalImages.get(0);
        Long imageIdToDelete = toDelete.id();
        performImageDelete("/api/v1/associations/" + associationId + "/raffles/" + raffleId + "/images/" + imageIdToDelete);

        // 2. Check that the deleted image is not in DB
        Optional<Image> deleted = imagesRepository.findById(imageIdToDelete);
        assertThat(deleted).isEmpty();

        // 3. Check that the file is removed from the filesystem
        Path deletedPath = Paths.get(toDelete.filePath());
        assertThat(Files.exists(deletedPath)).isFalse();

        // 4. Reorder remaining images
        List<ImageDTO> reordered = new ArrayList<>();
        int order = 1;
        for (ImageDTO image : originalImages) {
            if (!image.id().equals(imageIdToDelete)) {
                reordered.add(copyWithNewOrder(image, order++));
            }
        }

        // 5. Update the raffle with remaining images
        RaffleEdit editAfterDelete = RaffleEdit.builder()
                .images(reordered)
                .build();

        MvcResult result = performEditRaffleRequest(raffleId, editAfterDelete)
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Long raffleId = json.path("data").path("id").asLong();
        Raffle saved = rafflesRepository.findById(raffleId).orElseThrow();

        // 6. Check that the deleted image is not associated to the raffle
        List<Image> raffleImages = imagesRepository.findAllByRaffle(saved);
        assertThat(raffleImages.stream().noneMatch(img -> img.getId().equals(imageIdToDelete))).isTrue();

        // 7. Check that the stored images match the reordered list
        assertThat(raffleImages.size()).isEqualTo(reordered.size());
        for (int i = 0; i < raffleImages.size(); i++) {
            assertThat(raffleImages.get(i).getImageOrder()).isEqualTo(i + 1);
            assertThat(raffleImages.get(i).getRaffle().getId()).isEqualTo(raffleId);
            assertThat(Files.exists(Paths.get(raffleImages.get(i).getFilePath()))).isTrue();
        }
    }

    @Test
    @Transactional
    void shouldFailWhenTotalTicketsIsLessThanSold() throws Exception {
        Raffle raffle = rafflesRepository.findById(raffleId).orElseThrow();
        raffle.getTickets().forEach(ticket -> ticket.setStatus(SOLD));
        raffle.setSoldTickets((long) raffle.getTickets().size());
        rafflesRepository.save(raffle);

        RaffleEdit edit = RaffleEdit.builder()
                .totalTickets(2L)
                .images(parseImagesFromResponse(uploadImages(2).andReturn()))
                .build();

        performEditRaffleRequest(raffleId, edit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The total tickets count cannot be less than the number of tickets already sold for this raffle"));
    }

    private ResultActions performEditRaffleRequest(Long id, RaffleEdit edit) throws Exception {
        return mockMvc.perform(put("/api/v1/associations/" + associationId + "/raffles/" + id)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(edit)));
    }
}
