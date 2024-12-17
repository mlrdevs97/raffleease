package com.raffleease.raffleease.Domains.Associations.Mappers;

import com.raffleease.raffleease.Domains.Associations.DTO.AssociationCreate;
import com.raffleease.raffleease.Domains.Associations.DTO.AssociationDTO;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssociationsMapper {
    private final AddressMapper addressMapper;

    public Association toAssociation(AssociationCreate DTO) {
        return Association.builder()
                .name(DTO.name())
                .email(DTO.email())
                .phoneNumber(DTO.phoneNumber())
                .address(addressMapper.toAddress(DTO))
                .build();
    }

    public AssociationDTO fromAssociation(Association association) {
        return AssociationDTO.builder()
                .id(association.getId())
                .name(association.getName())
                .email(association.getEmail())
                .phoneNumber(association.getPhoneNumber())
                .address(addressMapper.fromAddress(association.getAddress()))
                .build();
    }
}