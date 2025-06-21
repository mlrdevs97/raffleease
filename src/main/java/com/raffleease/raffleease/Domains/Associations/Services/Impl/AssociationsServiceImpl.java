package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Mappers.AssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsMembershipsRepository;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterAssociationData;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.UniqueConstraintViolationException;
import com.raffleease.raffleease.Common.Utils.ConstraintViolationParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AssociationsServiceImpl implements AssociationsService {
    private final AssociationsRepository associationsRepository;
    private final AssociationsMembershipsRepository membershipsRepository;
    private final AssociationsMapper mapper;

    @Transactional
    @Override
    public Association create(RegisterAssociationData associationData) {
        return save(mapper.toAssociation(associationData));
    }

    @Override
    public Association findById(Long id) {
        return associationsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Association with id <" + id + "> not found"));
    }

    @Override
    public void createMembership(Association association, User user, AssociationRole role) {
        AssociationMembership membership = save(AssociationMembership.builder()
                .association(association)
                .user(user)
                .role(role)
                .build());
        association.getMemberships().add(membership);
        save(association);
    }

    private Association save(Association entity) {
        try {
            return associationsRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            Optional<String> constraintName = ConstraintViolationParser.extractConstraintName(ex);
            if (constraintName.isPresent()) {
                throw new UniqueConstraintViolationException(constraintName.get(), "Unique constraint violated: " + constraintName.get());
            } else {
                throw ex;
            }
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving association: " + ex.getMessage());
        }
    }

    private AssociationMembership save(AssociationMembership membership) {
        try {
            return membershipsRepository.save(membership);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving new membership for association: " + ex.getMessage());
        }
    }
}
