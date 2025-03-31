package com.raffleease.raffleease.Domains.Associations.Model;

import com.raffleease.raffleease.Domains.Users.Model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "Association_memberships")
public class AssociationMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Association association;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssociationRole role;
}
