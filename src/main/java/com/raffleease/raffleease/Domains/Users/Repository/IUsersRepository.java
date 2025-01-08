package com.raffleease.raffleease.Domains.Users.Repository;

import com.raffleease.raffleease.Domains.Users.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUsersRepository extends JpaRepository<User, Long> {
}
