package com.raffleease.raffleease.Common.Aspects;

import com.raffleease.raffleease.Domains.Associations.Model.AssociationRole;
import com.raffleease.raffleease.Domains.Auth.Services.AuthorizationService;
import com.raffleease.raffleease.Domains.Auth.Validations.AdminOnly;
import com.raffleease.raffleease.Domains.Auth.Validations.PreventSelfDeletion;
import com.raffleease.raffleease.Domains.Auth.Validations.RequireRole;
import com.raffleease.raffleease.Domains.Auth.Validations.SelfAccessOnly;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.ADMIN;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class AuthorizationAspect {
    private final AuthorizationService authorizationService;

    @Before("@annotation(requireRole)")
    public void checkRequiredRole(JoinPoint joinPoint, RequireRole requireRole) {
        Long associationId = extractAssociationId(joinPoint);
        Long targetUserId = extractTargetUserId(joinPoint);
        
        // Check if user can modify the target user (considering self-access)
        if (targetUserId != null && requireRole.allowSelfAccess()) {
            if (!authorizationService.canModifyUser(associationId, targetUserId, true)) {
                authorizationService.requireRole(associationId, requireRole.value(), requireRole.message());
            }
        } else {
            // Standard role requirement check
            authorizationService.requireRole(associationId, requireRole.value(), requireRole.message());
        }
        
        log.debug("Role authorization passed for user attempting to access resource requiring {}", requireRole.value());
    }

    @Before("@annotation(adminOnly)")
    public void checkAdminOnly(JoinPoint joinPoint, AdminOnly adminOnly) {
        Long associationId = extractAssociationId(joinPoint);
        authorizationService.requireRole(associationId, ADMIN, adminOnly.message());
        log.debug("Admin-only authorization passed");
    }

    @Before("@annotation(preventSelfDeletion)")
    public void checkPreventSelfDeletion(JoinPoint joinPoint, PreventSelfDeletion preventSelfDeletion) {
        Long targetUserId = extractParameterValue(joinPoint, preventSelfDeletion.userIdParam(), Long.class);
        if (targetUserId != null) {
            authorizationService.preventSelfAction(targetUserId, preventSelfDeletion.message());
            log.debug("Self-deletion prevention check passed");
        }
    }

    @Before("@annotation(selfAccessOnly)")
    public void checkSelfAccessOnly(JoinPoint joinPoint, SelfAccessOnly selfAccessOnly) {
        Long targetUserId = extractParameterValue(joinPoint, selfAccessOnly.userIdParam(), Long.class);
        if (targetUserId != null) {
            if (!authorizationService.isSameUser(targetUserId)) {
                throw new com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException(
                    selfAccessOnly.message()
                );
            }
            log.debug("Self-access-only authorization passed");
        }
    }

    /**
     * Extract the associationId parameter from the method call
     */
    private Long extractAssociationId(JoinPoint joinPoint) {
        return extractParameterValue(joinPoint, "associationId", Long.class);
    }

    /**
     * Extract the userId or id parameter from the method call (for target user operations)
     */
    private Long extractTargetUserId(JoinPoint joinPoint) {
        return extractParameterValue(joinPoint, "userId", Long.class);
    }

    /**
     * Extract a parameter value by name and type from the method call
     */
    private <T> T extractParameterValue(JoinPoint joinPoint, String parameterName, Class<T> type) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            // Check if parameter has @PathVariable annotation with the desired name
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String pathVarName = pathVariable.value().isEmpty() ? pathVariable.name() : pathVariable.value();
                if (pathVarName.isEmpty()) {
                    pathVarName = parameter.getName();
                }
                if (parameterName.equals(pathVarName) && type.isAssignableFrom(parameter.getType())) {
                    return type.cast(args[i]);
                }
            }
            
            // Fallback: check parameter name directly
            if (parameterName.equals(parameter.getName()) && type.isAssignableFrom(parameter.getType())) {
                return type.cast(args[i]);
            }
        }
        
        return null;
    }
} 