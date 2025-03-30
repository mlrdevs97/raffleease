package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Associations.Model.Address;
import com.raffleease.raffleease.Domains.Associations.Model.Association;

public class AssociationBuilder {
    private String associationName = "Default Association";
    private String userName = "default_user";
    private String email = "default@example.com";
    private String phoneNumber = "+34123456789";
    private String password = "password";
    private String city = "Seville";
    private String province = "Seville";
    private String zipCode = "41001";

    public AssociationBuilder withAssociationName(String name) {
        this.associationName = name;
        return this;
    }

    public AssociationBuilder withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public AssociationBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public AssociationBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public AssociationBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public AssociationBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public AssociationBuilder withProvince(String province) {
        this.province = province;
        return this;
    }

    public AssociationBuilder withZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    public Association build() {
        Address address = new Address();
        address.setCity(city);
        address.setProvince(province);
        address.setZipCode(zipCode);

        Association association = new Association();
        association.setAssociationName(associationName);
        association.setUserName(userName);
        association.setEmail(email);
        association.setPhoneNumber(phoneNumber);
        association.setPassword(password);
        association.setAddress(address);

        return association;
    }
}
