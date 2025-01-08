package com.raffleease.raffleease.Domains.Associations.Mappers.Impl;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Mappers.IAddressMapper;
import com.raffleease.raffleease.Domains.Associations.Mappers.IAssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssociationsMapper implements IAssociationsMapper {
    private final IAddressMapper addressMapper;

    public Association toAssociation(AssociationRegister request, String encodedPassword) {
        return Association.builder()
                .associationName(request.name())
                .userName(request.email())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .address(addressMapper.toAddress(request))
                .password(encodedPassword)
                .build();
    }

    public AssociationDTO fromAssociation(Association association) {
        return AssociationDTO.builder()
                .id(association.getId())
                .name(association.getAssociationName())
                .email(association.getEmail())
                .phoneNumber(association.getPhoneNumber())
                .address(addressMapper.fromAddress(association.getAddress()))
                .build();
    }
}