package com.raffleease.raffleease.Domains.Users.Services.Impls;

import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Model.CostumUserDetails;
import com.raffleease.raffleease.Domains.Users.Services.IUsersService;
import com.raffleease.raffleease.Exceptions.CustomExceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final IUsersService userService;

    @Value("${spring.application.config.is_test}")
    private boolean test;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (test) log.debug("Attempting to load user by identifier: {}", identifier);
        try {
            User user = userService.findByIdentifier(identifier);
            if (test) log.debug("User found: {}", user.getId());
            return new CostumUserDetails(user, identifier);
        } catch (NotFoundException ex) {
            if (test) log.warn("User not found for identifier: {}", identifier);
            throw new UsernameNotFoundException(ex.getMessage());
        }
    }
}
