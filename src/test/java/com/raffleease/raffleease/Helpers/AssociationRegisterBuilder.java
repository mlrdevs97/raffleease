package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;

public class AssociationRegisterBuilder {
    private String password = "StrongPassw0rd!";
    private String confirmPassword = "StrongPassw0rd!";
    private String name = "Test Association";
    private String email = "testassociation@example.com";
    private String phoneNumber = "+34666000111";
    private String city = "Madrid";
    private String province = "Madrid";
    private String zipCode = "28001";

    public AssociationRegisterBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public AssociationRegisterBuilder withConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        return this;
    }

    public AssociationRegisterBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AssociationRegisterBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public AssociationRegisterBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public AssociationRegisterBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public AssociationRegisterBuilder withProvince(String province) {
        this.province = province;
        return this;
    }

    public AssociationRegisterBuilder withZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    public AssociationRegister build() {
        return AssociationRegister.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .password(password)
                .confirmPassword(confirmPassword)
                .city(city)
                .province(province)
                .zipCode(zipCode)
                .build();
    }
}
