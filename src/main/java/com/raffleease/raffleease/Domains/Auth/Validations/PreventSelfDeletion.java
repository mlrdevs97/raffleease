package com.raffleease.raffleease.Domains.Auth.Validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to prevent users from performing actions on themselves.
 * Used for deletion operations where self-deletion should be prevented.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventSelfDeletion {
    /**
     * The parameter name that contains the target user ID
     */
    String userIdParam() default "userId";
    
    /**
     * Custom message to be shown when self-deletion is attempted
     */
    String message() default "You cannot perform this action on your own account";
} 