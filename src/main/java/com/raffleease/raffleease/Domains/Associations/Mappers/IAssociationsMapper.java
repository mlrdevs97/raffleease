package com.raffleease.raffleease.Domains.Associations.Mappers;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;

public interface IAssociationsMapper {
    Association toAssociation(AssociationRegister request, String encodedPassword);

    AssociationDTO fromAssociation(Association association);
}
