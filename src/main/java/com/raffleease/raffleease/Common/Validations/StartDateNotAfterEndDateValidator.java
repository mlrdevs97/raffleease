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

        // Check if end date is at least 24 hours after start date
        LocalDateTime minimumEndDate = startDate.plusHours(24);
        if (endDate.isBefore(minimumEndDate)) {
            context.buildConstraintViolationWithTemplate("The end date must be at least 24 hours after the start date")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}