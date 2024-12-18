package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Mappers.AssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Repository.IAssociationsRepository;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationQueryService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AssociationsQueryService implements IAssociationQueryService {
    private final IAssociationsRepository repository;
    private final AssociationsMapper mapper;

    public AssociationDTO findById(Long id) {
        return mapper.fromAssociation(
                repository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Association with id <" + id + "> not found"))
        );
    }

    public boolean exists(Long id) {
        return repository.existsById(id);
    }
}
