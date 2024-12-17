package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrchestratorService {
    private final QueryService queryService;
    private final CreateService createService;

    public AssociationDTO create(@Valid AssociationCreate request) {
        return createService.create(request);
    }

    public AssociationDTO findById(long id) {
        return queryService.findById(id);
    }
}
