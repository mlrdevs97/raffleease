package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Mappers.AssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Repository.IAssociationsRepository;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationCreateService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.ConflictException;
import com.raffleease.raffleease.Exceptions.CustomExceptions.DatabaseException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AssociationCreateServiceImpl implements IAssociationCreateService {
    private final IAssociationsRepository repository;
    private final AssociationsMapper mapper;

    @Transactional
    public AssociationDTO create(AssociationCreate associationCreate) {
        Association association = mapper.toAssociation(associationCreate);
        Association savedAssociation = save(association);
        return mapper.fromAssociation(savedAssociation);
    }

    private Association save(Association association) {
        try {
            return repository.save(association);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Failed to save association due to unique constraint violation: " + ex.getMessage());
        } catch (DataAccessException exp) {
            throw new DatabaseException("Database error occurred while saving association: " + exp.getMessage());
        }
    }
}
