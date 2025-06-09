package com.raffleease.raffleease.util;

import com.raffleease.raffleease.Domains.Associations.Model.Address;
import com.raffleease.raffleease.Domains.Associations.Model.Association;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationMembership;
import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Images.Model.Image;
import com.raffleease.raffleease.Domains.Raffles.Model.Raffle;
import com.raffleease.raffleease.Domains.Users.Model.User;
import com.raffleease.raffleease.Domains.Users.Model.UserRole;

import java.util.ArrayList;

/**
 * Test data builder following the Builder pattern for creating test entities.
 * Provides sensible defaults and allows customization of specific fields.
 */
public class TestDataBuilder {

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static AssociationBuilder association() {
        return new AssociationBuilder();
    }

    public static AddressBuilder address() {
        return new AddressBuilder();
    }

    public static AssociationMembershipBuilder membership() {
        return new AssociationMembershipBuilder();
    }

    public static ImageBuilder image() {
        return new ImageBuilder();
    }

    public static class UserBuilder {
        private String firstName = "John";
        private String lastName = "Doe";
        private String userName = "johndoe";
        private String email = "john.doe@example.com";
        private String phoneNumber = "+1234567890";
        private UserRole userRole = UserRole.ASSOCIATION_MEMBER;
        private String password = "$2a$10$8K1p/3m4kNG6cZOsLZhxOuWyEZwEG4CqJ8Zz8J9hOqWyEZwEG4CqJ8";
        private boolean isEnabled = true;

        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserBuilder userRole(UserRole userRole) {
            this.userRole = userRole;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder enabled(boolean enabled) {
            this.isEnabled = enabled;
            return this;
        }

        public UserBuilder disabled() {
            this.isEnabled = false;
            return this;
        }

        public User build() {
            return User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .userName(userName)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .userRole(userRole)
                    .password(password)
                    .isEnabled(isEnabled)
                    .build();
        }
    }

    public static class AssociationBuilder {
        private String name = "Test Association";
        private String description = "A test association for integration testing";
        private String phoneNumber = "+1987654321";
        private String email = "contact@testassociation.com";
        private Address address;

        public AssociationBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AssociationBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AssociationBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public AssociationBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AssociationBuilder address(Address address) {
            this.address = address;
            return this;
        }

        public Association build() {
            return Association.builder()
                    .name(name)
                    .description(description)
                    .phoneNumber(phoneNumber)
                    .email(email)
                    .address(address != null ? address : TestDataBuilder.address().build())
                    .memberships(new ArrayList<>())
                    .raffles(new ArrayList<>())
                    .build();
        }
    }

    public static class AddressBuilder {
        private String placeId = "ChIJOwg_06VPwokRYv534QaPC8g";
        private String formattedAddress = "New York, NY, USA";
        private Double latitude = 40.7128;
        private Double longitude = -74.0060;
        private String city = "New York";
        private String province = "NY";
        private String zipCode = "10001";

        public AddressBuilder placeId(String placeId) {
            this.placeId = placeId;
            return this;
        }

        public AddressBuilder formattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
            return this;
        }

        public AddressBuilder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public AddressBuilder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public AddressBuilder city(String city) {
            this.city = city;
            return this;
        }

        public AddressBuilder province(String province) {
            this.province = province;
            return this;
        }

        public AddressBuilder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Address build() {
            return Address.builder()
                    .placeId(placeId)
                    .formattedAddress(formattedAddress)
                    .latitude(latitude)
                    .longitude(longitude)
                    .city(city)
                    .province(province)
                    .zipCode(zipCode)
                    .build();
        }
    }

    public static class AssociationMembershipBuilder {
        private User user;
        private Association association;
        private AssociationRole role = AssociationRole.ADMIN;

        public AssociationMembershipBuilder user(User user) {
            this.user = user;
            return this;
        }

        public AssociationMembershipBuilder association(Association association) {
            this.association = association;
            return this;
        }

        public AssociationMembershipBuilder role(AssociationRole role) {
            this.role = role;
            return this;
        }

        public AssociationMembershipBuilder adminRole() {
            this.role = AssociationRole.ADMIN;
            return this;
        }

        public AssociationMembershipBuilder memberRole() {
            this.role = AssociationRole.MEMBER;
            return this;
        }

        public AssociationMembership build() {
            return AssociationMembership.builder()
                    .user(user)
                    .association(association)
                    .role(role)
                    .build();
        }
    }

    public static class ImageBuilder {
        private String fileName = "test-image.jpg";
        private String filePath = "/test/path/test-image.jpg";
        private String contentType = "image/jpeg";
        private String url = "http://localhost/test/images/1";
        private Integer imageOrder = 1;
        private Raffle raffle;
        private Association association;

        public ImageBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public ImageBuilder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public ImageBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ImageBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ImageBuilder imageOrder(Integer imageOrder) {
            this.imageOrder = imageOrder;
            return this;
        }

        public ImageBuilder raffle(Raffle raffle) {
            this.raffle = raffle;
            return this;
        }

        public ImageBuilder association(Association association) {
            this.association = association;
            return this;
        }

        public ImageBuilder pendingImage() {
            this.raffle = null;
            return this;
        }

        public ImageBuilder jpegImage() {
            this.contentType = "image/jpeg";
            this.fileName = "test-image.jpg";
            return this;
        }

        public ImageBuilder pngImage() {
            this.contentType = "image/png";
            this.fileName = "test-image.png";
            return this;
        }

        public Image build() {
            return Image.builder()
                    .fileName(fileName)
                    .filePath(filePath)
                    .contentType(contentType)
                    .url(url)
                    .imageOrder(imageOrder)
                    .raffle(raffle)
                    .association(association)
                    .build();
        }
    }
} 