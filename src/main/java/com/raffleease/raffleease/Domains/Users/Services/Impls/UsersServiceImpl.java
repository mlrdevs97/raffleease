package com.raffleease.raffleease.Domains.Users.Services.Impls;

import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.ICustomRepository;
import com.raffleease.raffleease.Domains.Users.Repository.IUsersRepository;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UsersServiceImpl implements IUsersService {
    private final IUsersRepository repository;
    private final ICustomRepository customRepository;

    @Value("${spring.application.config.is_test}")
    private boolean test;

    @Override
    public User findByIdentifier(String identifier) {
        if (test) log.debug("Searching user by identifier: {}", identifier);
        return customRepository.findByIdentifier(identifier).orElseThrow(() -> {
            if (test) log.warn("User not found with identifier: {}", identifier);
            return new NotFoundException("User not found with identifier: " + identifier);
        });
    }
    @Override
    public User findById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found with id: " + id)
        );
    }

    @Override
    public boolean existsById(Long id) {
        try {
            findById(id);
            return true;
        } catch (NotFoundException ex) {
            return false;
        }
    }
}
