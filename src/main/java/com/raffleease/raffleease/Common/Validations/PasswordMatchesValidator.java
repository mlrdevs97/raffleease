package com.raffleease.raffleease.Common.Validations;

import com.raffleease.raffleease.Common.Models.CreateUserData;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request == null) return true;

        CreateUserData userData = (CreateUserData) request;
        String password = ((CreateUserData) request).getPassword();
        String confirmPassword = ((CreateUserData) request).getConfirmPassword();

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