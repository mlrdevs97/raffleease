package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Mappers.IAssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsRepository;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsService;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensManagementService;
import com.raffleease.raffleease.Domains.Tokens.Services.TokensQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.ConflictException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AssociationsServiceImpl implements AssociationsService {
    private final AssociationsRepository repository;
    private final IAssociationsMapper mapper;
    private final TokensManagementService tokensManagementService;
    private final TokensQueryService tokensQueryService;

    @Transactional
    @Override
    public Association create(AssociationRegister request, String encodedPassword) {
        Association association = mapper.toAssociation(request, encodedPassword);
        return save(association);
    }

    @Override
    public Association findFromRequest(HttpServletRequest request) {
        String token = tokensManagementService.extractTokenFromRequest(request);
        String subject = tokensQueryService.getSubject(token);
        Long id = Long.parseLong(subject);
        return findById(id);
    }

    @Override
    public Association findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Association with id <" + id + "> not found"));
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
}
