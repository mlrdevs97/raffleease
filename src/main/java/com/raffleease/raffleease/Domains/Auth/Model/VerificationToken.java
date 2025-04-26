package com.raffleease.raffleease.Domains.Auth.Model;

import com.raffleease.raffleease.Domains.Users.Model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String token;

    @OneToOne
    private User user;

    private LocalDateTime expiryDate;
}
