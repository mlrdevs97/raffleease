package com.raffleease.raffleease.Domains.Users.Mappers;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterUserData;
import com.raffleease.raffleease.Domains.Users.Model.User;

public interface UsersMapper {
    User fromRegisterUserData(RegisterUserData data, String encodedPassword);
}
