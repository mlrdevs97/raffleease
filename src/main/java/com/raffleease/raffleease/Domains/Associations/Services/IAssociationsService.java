package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;

public interface IAssociationsService {
    Association create(AssociationRegister request, String encodedPassword);
    Association findById(Long id);
}
