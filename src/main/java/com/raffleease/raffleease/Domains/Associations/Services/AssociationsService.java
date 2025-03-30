package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import jakarta.servlet.http.HttpServletRequest;

public interface AssociationsService {
    Association create(AssociationRegister request, String encodedPassword);
    Association findFromRequest(HttpServletRequest request);
    Association findById(Long id);
}
