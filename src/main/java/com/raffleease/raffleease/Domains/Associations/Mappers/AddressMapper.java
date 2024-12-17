package com.raffleease.raffleease.Domains.Associations.Mappers;

import com.raffleease.raffleease.Domains.Associations.DTO.AddressDTO;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.Model.Address;
import org.springframework.stereotype.Service;

@Service
public class AddressMapper {
    public AddressDTO fromAddress(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .city(address.getCity())
                .province(address.getProvince())
                .zipCode(address.getZipCode())
                .build();
    }

    public Address toAddress(AssociationCreate associationCreate) {
        return Address.builder()
                .city(associationCreate.city())
                .province(associationCreate.province())
                .zipCode(associationCreate.zipCode())
                .build();
    }
}