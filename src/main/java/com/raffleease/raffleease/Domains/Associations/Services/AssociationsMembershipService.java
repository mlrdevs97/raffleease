package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface AssociationsMembershipService {
    void validateIsMember(Association association, User user);
    AssociationMembership findByUser(User user);
    AssociationRole getUserRoleInAssociation(User user);
}