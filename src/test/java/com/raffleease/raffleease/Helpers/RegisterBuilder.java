package com.raffleease.raffleease.Helpers;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.*;
import lombok.Getter;

@Getter
public class RegisterBuilder {
    // User data
    private String firstName = "Firstname";
    private String lastName = "Last Name";
    private String userName = "test_user";
    private String userEmail = "user@example.com";
    private String userPhonePrefix = "+34";
    private String userPhoneNumber = "600001111";
    private String password = "StrongPassw0rd!";
    private String confirmPassword = "StrongPassw0rd!";

    // Association data
    private String associationName = "Test Association";
    private String associationEmail = "association@example.com";
    private String associationPhonePrefix = "+34";
    private String associationPhoneNumber = "600009999";
    private String description = "Helping the world!";
    private String placeId = "ChIJ1234567890";
    private String formattedAddress = "123 Main Street, Madrid";
    private Double latitude = 40.4168;
    private Double longitude = -3.7038;
    private String city = "Madrid";
    private String province = "Madrid";
    private String zipCode = "28001";

    public RegisterBuilder withUserFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public RegisterBuilder withUserLastName(String lastName) {
        this.firstName = lastName;
        return this;
    }

    public RegisterBuilder withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public RegisterBuilder withUserEmail(String email) {
        this.userEmail = email;
        return this;
    }

    public RegisterBuilder withUserPhone(String prefix, String number) {
        this.userPhonePrefix = prefix;
        this.userPhoneNumber = number;
        return this;
    }

    public RegisterBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public RegisterBuilder withConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        return this;
    }

    public RegisterBuilder withAssociationName(String name) {
        this.associationName = name;
        return this;
    }

    public RegisterBuilder withAssociationEmail(String email) {
        this.associationEmail = email;
        return this;
    }

    public RegisterBuilder withAssociationPhone(String prefix, String number) {
        this.associationPhonePrefix = prefix;
        this.associationPhoneNumber = number;
        return this;
    }

    public RegisterBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public RegisterBuilder withAddress(String placeId, String formattedAddress, Double lat, Double lon, String city, String province, String zip) {
        this.placeId = placeId;
        this.formattedAddress = formattedAddress;
        this.latitude = lat;
        this.longitude = lon;
        this.city = city;
        this.province = province;
        this.zipCode = zip;
        return this;
    }

    public RegisterRequest build() {
        return RegisterRequest.builder()
                .userData(RegisterUserData.builder()
                                .firstName(firstName)
                                .lastName(lastName)
                                .userName(userName)
                                .email(userEmail)
                                .phoneNumber((userPhonePrefix != null || userPhoneNumber != null)
                                        ? PhoneNumberData.builder()
                                            .prefix(userPhonePrefix)
                                            .nationalNumber(userPhoneNumber)
                                            .build()
                                        : null)
                            .password(password)
                            .confirmPassword(confirmPassword)
                            .build())
                .associationData(RegisterAssociationData.builder()
                        .associationName(associationName)
                        .description(description)
                        .email(associationEmail)
                        .phoneNumber(PhoneNumberData.builder()
                                .prefix(associationPhonePrefix)
                                .nationalNumber(associationPhoneNumber)
                                .build())
                        .addressData(RegisterAddressData.builder()
                                .placeId(placeId)
                                .latitude(latitude)
                                .longitude(longitude)
                                .city(city)
                                .province(province)
                                .zipCode(zipCode)
                                .formattedAddress(formattedAddress)
                                .build())
                        .build())
                .build();
    }
}