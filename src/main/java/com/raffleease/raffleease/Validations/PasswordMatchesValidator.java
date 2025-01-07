package com.raffleease.raffleease.Validations;

import com.raffleease.raffleease.Domains.Auth.DTOs.AssociationRegister;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        AssociationRegister associationRegister = (AssociationRegister) request;
        boolean passwordsMatch = associationRegister.password().equals(associationRegister.confirmPassword());
        if (!passwordsMatch) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords and confirm password don't match")
                    .addPropertyNode("confirmPassword").addConstraintViolation();
        }
        return passwordsMatch;
    }
}