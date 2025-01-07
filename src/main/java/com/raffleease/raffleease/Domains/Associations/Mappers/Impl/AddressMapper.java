package com.raffleease.raffleease.Domains.Associations.Mappers.Impl;

import com.raffleease.raffleease.Domains.Associations.DTO.AddressDTO;
import com.raffleease.raffleease.Domains.Associations.Mappers.IAddressMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Address;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import org.springframework.stereotype.Service;

@Service
public class AddressMapper implements IAddressMapper {
    public Address toAddress(AssociationRegister request) {
        return Address.builder()
                .city(request.city())
                .province(request.province())
                .zipCode(request.zipCode())
                .build();
    }

    public AddressDTO fromAddress(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .city(address.getCity())
                .province(address.getProvince())
                .zipCode(address.getZipCode())
                .build();
    }
}