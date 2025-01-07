package com.raffleease.raffleease.Domains.Associations.Mappers;

import com.raffleease.raffleease.Domains.Associations.DTO.AddressDTO;
import com.raffleease.raffleease.Domains.Associations.Model.Address;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;

public interface IAddressMapper {
    Address toAddress(AssociationRegister request);
    AddressDTO fromAddress(Address address);
}
