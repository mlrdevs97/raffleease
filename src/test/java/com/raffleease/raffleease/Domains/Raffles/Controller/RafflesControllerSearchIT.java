package com.raffleease.raffleease.Domains.Raffles.Controller;

import com.raffleease.raffleease.Domains.Images.DTOs.ImageDTO;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Raffles.Model.RaffleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class RafflesControllerSearchIT extends BaseRafflesIT {
    private String rafflesPath;
    private List<Long> raffleIds;

    @BeforeEach
    void setUp() throws Exception {
        rafflesPath = "/api/v1/associations/" + associationId + "/raffles";
        raffleIds = new ArrayList<>();
        raffleIds.add(createRaffleWith("Alpha Raffle", RaffleStatus.PENDING, LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(20)));
        raffleIds.add(createRaffleWith("Beta Raffle", RaffleStatus.ACTIVE, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(15)));
        raffleIds.add(createRaffleWith("Gamma Raffle", RaffleStatus.COMPLETED, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
    }

    @Test
    void shouldReturnAllRafflesWithPagination() throws Exception {
        perGetRequest(Map.of("page", "0", "size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.page.size").value(2))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(2))
                .andExpect(jsonPath("$.message").value("Raffles retrieved successfully"));
    }

    @Test
    void shouldFilterByTitleContainingIgnoreCase() throws Exception {
        perGetRequest(Map.of("title", "alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Alpha Raffle"));
    }

    @Test
    void shouldFilterByStatus() throws Exception {
        perGetRequest(Map.of("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Beta Raffle"));
    }

    @Test
    void shouldCombineFilters() throws Exception {
        perGetRequest(Map.of("title", "gamma", "status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Gamma Raffle"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoRafflesMatch() throws Exception {
        perGetRequest(Map.of("title", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.page.totalElements").value(0))
                .andExpect(jsonPath("$.data.page.totalPages").value(0))
                .andExpect(jsonPath("$.message").value("Raffles retrieved successfully"));
    }

    @Test
    void shouldRespectPaginationWithFilters() throws Exception {
        perGetRequest(Map.of("title", "raffle", "page", "1", "size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page.size").value(2))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(2))
                .andExpect(jsonPath("$.data.page.number").value(1));
    }

    @Test
    void shouldSortByTitleAsc() throws Exception {
        perGetRequest(Map.of("sortBy", "title", "sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Alpha Raffle"))
                .andExpect(jsonPath("$.data.content[1].title").value("Beta Raffle"));
    }

    @Test
    void shouldSortByTitleDesc() throws Exception {
        perGetRequest(Map.of("sortBy", "title", "sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Gamma Raffle"))
                .andExpect(jsonPath("$.data.content[1].title").value("Beta Raffle"));
    }

    @Test
    void shouldSortByStartDateAsc() throws Exception {
        perGetRequest(Map.of("sortBy", "startDate", "sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Gamma Raffle"))
                .andExpect(jsonPath("$.data.content[1].title").value("Beta Raffle"));
    }

    @Test
    void shouldSortByEndDateDesc() throws Exception {
        perGetRequest(Map.of("sortBy", "endDate", "sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Alpha Raffle"))
                .andExpect(jsonPath("$.data.content[1].title").value("Beta Raffle"));
    }

    @Test
    void shouldDefaultToCreatedAtDescIfSortByInvalid() throws Exception {
        perGetRequest(Map.of("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(3));
    }

    @Test
    void shouldIgnoreUnknownQueryParameters() throws Exception {
        perGetRequest(Map.of("nonexistentFilter", "something"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(3));
    }

    @Test
    void shouldReturnAllRafflesWhenFiltersAreNullOrEmpty() throws Exception {
        perGetRequest(new HashMap<>())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page.totalElements").value(3));
    }

    @Test
    void shouldTrimWhitespaceInFilters() throws Exception {
        perGetRequest(Map.of("title", "   Alpha   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Alpha Raffle"));
    }

    private ResultActions perGetRequest(Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder request = get(rafflesPath).header(AUTHORIZATION, "Bearer " + accessToken);
        if (params != null) {
            params.forEach(request::param);
        }
        return mockMvc.perform(request);
    }

    private Long createRaffleWith(String title, RaffleStatus status, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        List<ImageDTO> images = parseImagesFromResponse(uploadImages(1).andReturn());
        Long id = createRaffle(images, associationId, accessToken);
        Raffle raffle = rafflesRepository.findById(id).orElseThrow();
        raffle.setTitle(title);
        raffle.setStatus(status);
        raffle.setStartDate(startDate);
        raffle.setEndDate(endDate);
        rafflesRepository.save(raffle);
        return id;
    }
} 