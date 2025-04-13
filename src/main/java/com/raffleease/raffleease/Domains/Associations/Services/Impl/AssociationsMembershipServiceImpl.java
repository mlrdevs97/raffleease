package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsMembershipsRepository;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsMembershipService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Exceptions.CustomExceptions.AuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AssociationsMembershipServiceImpl implements AssociationsMembershipService {
    private final AssociationsMembershipsRepository membershipsRepository;

    @Override
    public void validateIsMember(Association association, User user) {
        boolean isMember = membershipsRepository.existsByAssociationAndUser(association, user);
        if (!isMember) {
            throw new AuthorizationException("You are not a member of this association");
        }
    }
}
