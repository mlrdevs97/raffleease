package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterAssociationData;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface AssociationsService {
    Association create(RegisterAssociationData associationData);
    Association findById(Long id);
    void createMembership(Association association, User user, AssociationRole role);
}
