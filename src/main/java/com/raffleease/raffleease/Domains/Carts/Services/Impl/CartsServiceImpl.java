package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.CartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Repository.CartsRepository;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import com.raffleease.raffleease.Domains.Carts.Services.CartLifecycleService;
import com.raffleease.raffleease.Domains.Carts.Services.CartsPersistenceService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.ACTIVE;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartsServiceImpl implements CartsService {
    private final CartsRepository repository;
    private final CartsMapper mapper;
    private final UsersService usersService;
    private final CartLifecycleService cartLifecycleService;
    private final CartsPersistenceService cartsPersistenceService;

    @Override
    @Transactional
    public CartDTO create() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = usersService.findByIdentifier(auth.getName());
        closeUserActiveCart(user);

        log.info("Creating new cart for user: {}", user.getId());

        return mapper.fromCart(save(Cart.builder()
                .status(ACTIVE)
                .user(user)
                .tickets(new ArrayList<>())
                .build()));
    }

    @Override
    public CartDTO get(Long cartId) {
        return mapper.fromCart(cartsPersistenceService.findById(cartId));
    }

    @Override
    public CartDTO getUserCart() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identifier = auth.getName();
        User user = usersService.findByIdentifier(identifier);
        Cart cart = repository.findByUserAndStatus(user, ACTIVE)
                .orElseThrow(() -> new NotFoundException("Active cart not found for user with id <" + user.getId() + ">"));
        return mapper.fromCart(cart);
    }

    @Override
    public Cart save(Cart cart) {
        try {
            return repository.save(cart);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving cart: " + ex.getMessage());
        }
    }

    private void closeUserActiveCart(User user) {
        Optional<Cart> existingCart = repository.findByUserAndStatus(user, ACTIVE);
        if (existingCart.isEmpty()) {
            log.info("No existing cart for user found");
            return;
        }
        Cart cart = existingCart.get();

        log.info("Already existing cart found for user. Cart Id: {}", cart.getId());

        cartLifecycleService.releaseCart(cart);
        repository.flush();
    }
}
