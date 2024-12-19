package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationCreateService;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationQueryService;
import com.raffleease.raffleease.Domains.Associations.Services.IAssociationsOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AssociationsOrchestratorImpl implements IAssociationsOrchestrator {
    private final IAssociationQueryService queryService;
    private final IAssociationCreateService createService;
    public AssociationDTO create(AssociationCreate request) {
        return createService.create(request);
    }
    public AssociationDTO findById(long id) {
        return queryService.findById(id);
    }
}
