package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Mappers.AssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Repository.IAssociationsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CreateService {
    private final IAssociationsRepository repository;
    private final ValidationService validationService;
    private final AssociationsMapper mapper;

    @Transactional
    public AssociationDTO create(AssociationCreate associationCreate) {
        validationService.validateUniqueData(associationCreate);
        Association association = mapper.toAssociation(associationCreate);
        Association savedAssociation = save(association);
        return mapper.fromAssociation(savedAssociation);
    }

    private Association save(Association association) {
        try {
            return repository.save(association);
        } catch (Exception exp) {
            throw new DataBaseHandlingException("Error accessing database when saving association: " + exp.getMessage());
        }
    }
}
