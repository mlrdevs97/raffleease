package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Base.AbstractIntegrationTest;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import com.raffleease.raffleease.Domains.Raffles.Repository.RafflesRepository;
import com.raffleease.raffleease.util.AuthTestUtils;
import com.raffleease.raffleease.util.AuthTestUtils.AuthTestData;
import com.raffleease.raffleease.util.TestDataBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.*;
import static com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus.COMPLETED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@DisplayName("Raffles Search Controller Integration Tests")
class RafflesSearchControllerIT extends AbstractIntegrationTest {

    @Autowired
    private AuthTestUtils authTestUtils;

    @Autowired
    private RafflesRepository rafflesRepository;

    private AuthTestData authData;
    private String searchEndpoint;
    private List<Raffle> testRaffles;

    @BeforeEach
    void setUp() {
        authData = authTestUtils.createAuthenticatedUser();
        searchEndpoint = "/v1/associations/" + authData.association().getId() + "/raffles";
        
        // Create test raffles with different statuses and titles
        testRaffles = List.of(
            createTestRaffle("Summer Raffle", PENDING),
            createTestRaffle("Winter Raffle", ACTIVE),
            createTestRaffle("Spring Raffle", PAUSED),
            createTestRaffle("Autumn Raffle", COMPLETED)
        );
    }

    private Raffle createTestRaffle(String title, RaffleStatus status) {
        Raffle raffle = TestDataBuilder.raffle()
                .association(authData.association())
                .status(status)
                .title(title)
                .description("Test raffle for search")
                .endDate(LocalDateTime.now().plusDays(7))
                .build();
        return rafflesRepository.save(raffle);
    }

    @Nested
    @DisplayName("GET /v1/associations/{associationId}/raffles")
    class SearchRafflesTests {

        @Test
        @DisplayName("Should return all raffles for association when no filters applied")
        void shouldReturnAllRafflesForAssociation() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(4))
                    .andExpect(jsonPath("$.data.totalElements").value(4));
        }

        @Test
        @DisplayName("Should filter raffles by title")
        void shouldFilterRafflesByTitle() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("title", "Summer")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Summer Raffle"));
        }

        @Test
        @DisplayName("Should filter raffles by status")
        void shouldFilterRafflesByStatus() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("status", "ACTIVE")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should filter raffles by both title and status")
        void shouldFilterRafflesByTitleAndStatus() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("title", "Winter")
                    .param("status", "ACTIVE")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Winter Raffle"))
                    .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should support pagination")
        void shouldSupportPagination() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("page", "0")
                    .param("size", "2")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(4))
                    .andExpect(jsonPath("$.data.totalPages").value(2));
        }

        @Test
        @DisplayName("Should support sorting by title")
        void shouldSupportSortingByTitle() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("sort", "title,asc")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value("Autumn Raffle"))
                    .andExpect(jsonPath("$.data.content[1].title").value("Spring Raffle"));
        }

        @Test
        @DisplayName("Should support sorting by startDate")
        void shouldSupportSortingByStartDate() throws Exception {
            // Arrange - Set different start dates for all raffles
            LocalDateTime now = LocalDateTime.now();
            testRaffles.get(0).setStartDate(now.minusDays(2));
            testRaffles.get(1).setStartDate(now.minusDays(1));
            testRaffles.get(2).setStartDate(now.minusDays(3));
            testRaffles.get(3).setStartDate(now.minusDays(4));
            rafflesRepository.saveAll(testRaffles);

            // Log the dates for debugging
            testRaffles.forEach(raffle -> 
                log.info("Raffle: {}, StartDate: {}", raffle.getTitle(), raffle.getStartDate()));

            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("sort", "startDate,desc")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value("Winter Raffle"))
                    .andExpect(jsonPath("$.data.content[1].title").value("Summer Raffle"))
                    .andExpect(jsonPath("$.data.content[2].title").value("Spring Raffle"))
                    .andExpect(jsonPath("$.data.content[3].title").value("Autumn Raffle"));

            // Log the response for debugging
            String response = result.andReturn().getResponse().getContentAsString();
            log.info("Response: {}", response);
        }

        @Test
        @DisplayName("Should support sorting by endDate")
        void shouldSupportSortingByEndDate() throws Exception {
            // Arrange - Set different end dates for all raffles
            LocalDateTime now = LocalDateTime.now();
            testRaffles.get(0).setEndDate(now.plusDays(4));
            testRaffles.get(1).setEndDate(now.plusDays(2));
            testRaffles.get(2).setEndDate(now.plusDays(6));
            testRaffles.get(3).setEndDate(now.plusDays(8));
            rafflesRepository.saveAll(testRaffles);

            // Log the dates for debugging
            testRaffles.forEach(raffle -> 
                log.info("Raffle: {}, EndDate: {}", raffle.getTitle(), raffle.getEndDate()));

            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("sort", "endDate,asc")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value("Winter Raffle"))
                    .andExpect(jsonPath("$.data.content[1].title").value("Summer Raffle"))
                    .andExpect(jsonPath("$.data.content[2].title").value("Spring Raffle"))
                    .andExpect(jsonPath("$.data.content[3].title").value("Autumn Raffle"));

            // Log the response for debugging
            String response = result.andReturn().getResponse().getContentAsString();
            log.info("Response: {}", response);
        }

        @Test
        @DisplayName("Should return empty result when no matches found")
        void shouldReturnEmptyResultWhenNoMatches() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .param("title", "NonExistentRaffle")
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 when user doesn't belong to association")
        void shouldReturn403WhenUserDoesntBelongToAssociation() throws Exception {
            // Arrange
            AuthTestData otherUserData = authTestUtils.createAuthenticatedUserWithCredentials(
                    "otheruser", "other@example.com", "password123");

            // Act
            ResultActions result = mockMvc.perform(get(searchEndpoint)
                    .with(user(otherUserData.user().getEmail())));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when association doesn't exist")
        void shouldReturn404WhenAssociationDoesntExist() throws Exception {
            // Arrange
            String nonExistentAssociationEndpoint = "/v1/associations/99999/raffles";

            // Act
            ResultActions result = mockMvc.perform(get(nonExistentAssociationEndpoint)
                    .with(user(authData.user().getEmail())));

            // Assert
            result.andExpect(status().isForbidden());
        }
    }
} 