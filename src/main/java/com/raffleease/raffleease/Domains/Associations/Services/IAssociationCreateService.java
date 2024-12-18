package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;

public interface IAssociationCreateService {
    AssociationDTO create(AssociationCreate associationCreate);
}
