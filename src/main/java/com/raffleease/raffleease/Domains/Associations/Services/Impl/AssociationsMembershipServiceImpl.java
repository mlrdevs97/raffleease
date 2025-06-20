package com.raffleease.raffleease.Domains.Associations.Services.Impl;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Associations.Repository.AssociationsMembershipsRepository;
import com.raffleease.raffleease.Domains.Associations.Services.AssociationsMembershipService;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException;
import com.raffleease.raffleease.Common.Exceptions.CustomExceptions.NotFoundException;
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
            throw new AuthorizationException("User is not a member of the association");
        }
    }

    @Override
    public AssociationMembership findByUser(User user) {
        return membershipsRepository.findByUser(user).orElseThrow(
                () -> new NotFoundException("No association membership was found for user <" + user.getId() + ">")
        );
    }

    @Override
    public AssociationRole getUserRoleInAssociation(User user) {
        AssociationMembership membership = findByUser(user);
        return membership.getRole();
    }
}
