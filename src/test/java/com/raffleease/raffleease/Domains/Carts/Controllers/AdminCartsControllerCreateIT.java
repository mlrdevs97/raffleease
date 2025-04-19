package com.raffleease.raffleease.Domains.Carts.Controllers;

import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static com.raffleease.raffleease.Domains.Carts.Model.CartOwnerType.ADMIN;
import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.ACTIVE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.net.HttpHeaders.LOCATION;

public class AdminCartsControllerCreateIT extends BaseAdminCartsIT {
    @Test
    @Transactional
    void shouldCreateCart() throws Exception {
        MvcResult result = performCreateCartRequest(associationId, accessToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New cart created successfully"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(CREATED.value());
        String location = result.getResponse().getHeader(LOCATION);
        assertThat(location).isNotNull();
        assertThat(location).contains("/api/v1/associations/" + associationId + "/carts/");
        long cartId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
        Cart cart = cartsRepository.findById(cartId).orElseThrow();
        assertThat(cart.getStatus()).isEqualTo(ACTIVE);
        assertThat(cart.getOwnerType()).isEqualTo(ADMIN);
        assertThat(cart.getTickets().isEmpty()).isTrue();
        assertThat(cart.getCustomer()).isNull();
        assertThat(cart.getCreatedAt()).isNotNull();
        assertThat(cart.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFailCreateCartIfUserDoesNotBelongToAssociation() throws Exception {
        String otherToken = registerOtherUser().accessToken();
        performCreateCartRequest(associationId, otherToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not a member of this association"));
    }
}
