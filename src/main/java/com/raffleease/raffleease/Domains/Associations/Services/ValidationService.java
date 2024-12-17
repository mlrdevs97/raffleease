package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.Repository.IAssociationsRepository;
import com.raffleease.raffleease.Exceptions.CustomExceptions.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ValidationService {
    private final IAssociationsRepository repository;

    public void validateUniqueData(AssociationCreate associationCreate) {
        if (repository.existsByName(associationCreate.name())) {
            throw new ConflictException("Association's name already exists");
        }
        if (repository.existsByEmail(associationCreate.email())) {
            throw new ConflictException("Association's email already exists");
        }
        if (repository.existsByPhoneNumber(associationCreate.phoneNumber())) {
            throw new ConflictException("Association's phone number already exists");
        }
    }
}
