package com.raffleease.raffleease.Domains.Users.Mappers.Impls;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterUserData;
import com.raffleease.raffleease.Domains.Users.Mappers.UsersMapper;
import com.raffleease.raffleease.Domains.Users.Model.User;
import org.springframework.stereotype.Service;

@Service
public class UsersMapperImpl implements UsersMapper {
    @Override
    public User fromRegisterUserData(RegisterUserData data, String encodedPassword) {
        return User.builder()
                .userName(data.userName())
                .email(data.email())
                .phoneNumber(data.phoneNumber().prefix() + data.phoneNumber().nationalNumber())
                .password(encodedPassword)
                .build();
    }
}
