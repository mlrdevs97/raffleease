package com.raffleease.raffleease.Domains.Associations.Mappers.Impl;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Mappers.IAddressMapper;
import com.raffleease.raffleease.Domains.Associations.Mappers.IAssociationsMapper;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterAssociationData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AssociationsMapper implements IAssociationsMapper {
    private final IAddressMapper addressMapper;

    public Association toAssociation(RegisterAssociationData associationData) {
        return Association.builder()
                .name(associationData.associationName())
                .email(associationData.email())
                .phoneNumber(associationData.phoneNumber().prefix() + associationData.phoneNumber().nationalNumber())
                .address(addressMapper.toAddress(associationData.addressData()))
                .memberships(new ArrayList<>())
                .build();
    }

    public AssociationDTO fromAssociation(Association association) {
        return AssociationDTO.builder()
                .id(association.getId())
                .name(association.getName())
                .email(association.getEmail())
                .phoneNumber(association.getPhoneNumber().toString())
                .address(addressMapper.fromAddress(association.getAddress()))
                .build();
    }
}