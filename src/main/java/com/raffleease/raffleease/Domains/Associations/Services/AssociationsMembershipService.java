package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface AssociationsMembershipService {
    void validateIsMember(Association association, User user);
}
