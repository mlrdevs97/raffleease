package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;

public interface IAssociationQueryService {
    AssociationDTO findById(Long id);
    boolean exists(Long id);
}
