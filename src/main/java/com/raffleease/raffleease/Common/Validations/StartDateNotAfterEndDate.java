package com.raffleease.raffleease.Common.Validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = StartDateNotAfterEndDateValidator.class)
public @interface StartDateNotAfterEndDate {
    String message() default "Start time cannot be after end time";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}

