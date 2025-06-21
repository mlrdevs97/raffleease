package com.raffleease.raffleease.Domains.Associations.Services;

import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface AssociationsMembershipService {
    /*
     * Validates if the user is a member of the association.
     * 
     * @param association the association
     * @param user the user
     * @throws AuthorizationException if the user is not a member of the association
     */
    void validateIsMember(Association association, User user);

    /*
     * Finds the membership of the user in the association.
     * 
     * @param user the user
     * @return the membership
     * @throws NotFoundException if the user is not a member of the association
     */
    AssociationMembership findByUser(User user);

    /*
     * Gets the role of the user in the association.
     * 
     * @param user the user
     * @return the role
     * @throws NotFoundException if the user is not a member of the association
     */
    AssociationRole getUserRoleInAssociation(User user);
}