package com.raffleease.raffleease.Domains.Associations.Mappers;

import com.raffleease.raffleease.Domains.Associations.DTO.AddressDTO;
import com.raffleease.raffleease.Domains.Associations.Model.Address;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterAddressData;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;

public interface IAddressMapper {
    Address toAddress(RegisterAddressData addressData);
    AddressDTO fromAddress(Address address);
}
