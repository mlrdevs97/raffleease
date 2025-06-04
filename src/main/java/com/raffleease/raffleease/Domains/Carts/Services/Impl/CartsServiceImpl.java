package com.raffleease.raffleease.Domains.Carts.Services.Impl;

import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.BusinessException;
import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.CartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Carts.Repository.CartsRepository;
import com.raffleease.raffleease.Domains.Carts.Services.CartsService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Optional;

import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.ACTIVE;
import static com.raffleease.raffleease.Domains.Carts.Model.CartStatus.CLOSED;

@RequiredArgsConstructor
@Service
public class CartsServiceImpl implements CartsService {
    private final CartsRepository repository;
    private final CartsMapper mapper;
    private final UsersService usersService;
    
    @Override
    public CartDTO create() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = usersService.findByIdentifier(auth.getName());
        closeUserActiveCart(user);
        return mapper.fromCart(save(Cart.builder()
                .status(ACTIVE)
                .user(user)
                .tickets(new ArrayList<>())
                .build()));
    }

    @Override
    public Cart findById(Long id) {
        try {
            return repository.findById(id).orElseThrow(() -> new NotFoundException("Cart not found for id <" + id + ">"));
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while fetching cart with ID <" + id + ">: " + ex.getMessage());
        }
    }

    @Override
    public CartDTO get(Long cartId) {
        return mapper.fromCart(findById(cartId));
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
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage().contains("uk_user_active_cart")) {
                throw new BusinessException("User already has an active cart.");
            }
            throw new DatabaseException("Database error occurred while saving cart: " + ex.getMessage());
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving cart: " + ex.getMessage());
        }
    }

    private void closeUserActiveCart(User user) {
        Optional<Cart> existingCart = repository.findByUserAndStatus(user, ACTIVE);
        if (existingCart.isEmpty()) {
            return;
        }
        Cart cart = existingCart.get();
        cart.setStatus(CLOSED);
        save(cart);
    }
}
