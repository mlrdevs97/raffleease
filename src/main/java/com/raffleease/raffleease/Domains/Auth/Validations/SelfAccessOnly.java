package com.raffleease.raffleease.Domains.Auth.Validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that can only be accessed by the user themselves.
 * Unlike @RequireRole with allowSelfAccess=true, this prevents even administrators 
 * from accessing other users' resources.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SelfAccessOnly {
    /**
     * The parameter name that contains the target user ID
     */
    String userIdParam() default "userId";
    
    /**
     * Custom message to be shown when access is denied
     */
    String message() default "You can only access your own account for this action";
} 