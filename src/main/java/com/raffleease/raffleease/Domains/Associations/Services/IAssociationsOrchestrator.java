package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import jakarta.validation.Valid;

public interface IAssociationsOrchestrator {
    AssociationDTO create(@Valid AssociationCreate request);
    AssociationDTO findById(long id);
}
