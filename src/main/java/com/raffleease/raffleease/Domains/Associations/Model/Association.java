package com.raffleease.raffleease.Domains.Associations.Model;

import com.raffleease.raffleease.Domains.Users.Model.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "Associations")
public class Association extends User {
    @Column(nullable = false, unique = true)
    private String associationName;

    @OneToOne(cascade = CascadeType.ALL)
    private Address address;
}