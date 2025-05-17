package com.raffleease.raffleease.Domains.Users.Services.Impls;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterUserData;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Repository.UsersRepository;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.UniqueConstraintViolationException;
import com.raffleease.raffleease.Common.Utils.ConstraintViolationParser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.raffleease.raffleease.Domains.Users.Model.UserRole.ASSOCIATION_MEMBER;

@RequiredArgsConstructor
@Service
public class UsersServiceImpl implements UsersService {
    private final UsersRepository repository;

    @Override
    public User create(RegisterUserData userData, String encodedPassword) {
        return save(buildUser(userData, encodedPassword));
    }

    @Override
    public User enableUser(User user) {
        user.setEnabled(true);
        return save(user);
    }

    @Override
    public User findByIdentifier(String identifier) {
        return repository.findByIdentifier(identifier).orElseThrow(
                () -> new NotFoundException("User not found with identifier: " + identifier)
        );
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

    private User buildUser(RegisterUserData data, String encodedPassword) {
        String phoneNumber = Objects.nonNull(data.phoneNumber())
                ? data.phoneNumber().prefix() + data.phoneNumber().nationalNumber()
                : null;

        return User.builder()
                .firstName(data.firstName())
                .lastName(data.lastName())
                .userRole(ASSOCIATION_MEMBER)
                .userName(data.userName())
                .email(data.email())
                .phoneNumber(phoneNumber)
                .password(encodedPassword)
                .build();
    }

    private User save(User user) {
        try {
            return repository.save(user);
        } catch (DataIntegrityViolationException ex) {
            Optional<String> constraintName = ConstraintViolationParser.extractConstraintName(ex);

            if (constraintName.isPresent()) {
                throw new UniqueConstraintViolationException(constraintName.get(), "Unique constraint violated: " + constraintName.get());
            } else {
                throw ex;
            }
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving user: " + ex.getMessage());
        }
    }
}
