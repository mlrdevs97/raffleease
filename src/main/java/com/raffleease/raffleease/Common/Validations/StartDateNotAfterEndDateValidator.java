package com.raffleease.raffleease.Common.Validations;

import com.raffleease.raffleease.Domains.Raffles.DTOs.RaffleCreate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class StartDateNotAfterEndDateValidator implements ConstraintValidator<StartDateNotAfterEndDate, RaffleCreate> {
    @Override
    public boolean isValid(RaffleCreate raffleData, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        LocalDateTime startDate = raffleData.startDate();
        LocalDateTime endDate = raffleData.endDate();

        if (startDate == null || endDate == null) {
            return true;
        }

        if (startDate.isEqual(endDate) || startDate.isAfter(endDate)) {
            context.buildConstraintViolationWithTemplate(startDate + " must be before or equal to " + endDate)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}