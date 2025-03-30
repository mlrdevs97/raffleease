package com.raffleease.raffleease.Validations;

import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        AssociationRegister associationRegister = (AssociationRegister) request;
        String password = associationRegister.password();
        String confirmPassword = associationRegister.confirmPassword();

        if (password == null || confirmPassword == null) return true;

        boolean passwordsMatch = password.equals(confirmPassword);
        if (!passwordsMatch) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password and confirm password don't match")
                    .addPropertyNode("confirmPassword").addConstraintViolation();
        }
        return passwordsMatch;
    }
}