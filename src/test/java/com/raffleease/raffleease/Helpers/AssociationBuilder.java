package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Associations.Model.Address;
import com.raffleease.raffleease.Domains.Associations.Model.Association;

public class AssociationBuilder {
    private String name = "Test Association";
    private String email = "association@example.com";
    private String phoneNumber = "+34660009999";
    private String description = "Helping the world!";
    private Address address = Address.builder()
            .placeId("ChIJ1234567890")
            .formattedAddress("123 Main Street, Madrid")
            .latitude(40.4168)
            .longitude(-3.7038)
            .city("Madrid")
            .province("Madrid")
            .zipCode("28001")
            .build();

    public AssociationBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AssociationBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public AssociationBuilder withPhoneNumber(String value) {
        this.phoneNumber = value;
        return this;
    }

    public AssociationBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public AssociationBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }

    public Association build() {
        return Association.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .description(description)
                .address(address)
                .build();
    }
}
