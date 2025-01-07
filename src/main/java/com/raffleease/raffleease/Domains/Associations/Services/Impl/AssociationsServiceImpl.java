package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Mappers.IAssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Repository.IAssociationsRepository;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Exceptions.CustomExceptions.ConflictException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AssociationsServiceImpl implements IAssociationsService {
    private final IAssociationsRepository repository;
    private final IAssociationsMapper mapper;

    @Transactional
    @Override
    public Association create(AssociationRegister request, String encodedPassword) {
        Association association = mapper.toAssociation(request, encodedPassword);
        return save(association);
    }

    private Association save(Association entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Failed to save association due to unique constraint violation: " + ex.getMessage());
        } catch (DataAccessException ex) {
            throw new DatabaseException("Database error occurred while saving association: " + ex.getMessage());
        }
    }

    @Override
    public Association findByIdentifier(String identifier) {
        return repository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundException("Association with identifier <" + identifier + "> not found"));
    }
}
