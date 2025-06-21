package com.raffleease.raffleease.Common.Aspects;

import com.raffleease.raffleease.Common.Utils.AspectUtils;
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
import org.springframework.stereotype.Component;

import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.ADMIN;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class AuthorizationAspect {
    private final AuthorizationService authorizationService;

    @Before("@annotation(requireRole)")
    public void checkRequiredRole(JoinPoint joinPoint, RequireRole requireRole) {
        Long associationId = AspectUtils.extractAssociationId(joinPoint);
        Long targetUserId = AspectUtils.extractUserId(joinPoint);
        
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
        Long associationId = AspectUtils.extractAssociationId(joinPoint);
        authorizationService.requireRole(associationId, ADMIN, adminOnly.message());
        log.debug("Admin-only authorization passed");
    }

    @Before("@annotation(preventSelfDeletion)")
    public void checkPreventSelfDeletion(JoinPoint joinPoint, PreventSelfDeletion preventSelfDeletion) {
        Long targetUserId = AspectUtils.extractParameterValue(joinPoint, preventSelfDeletion.userIdParam(), Long.class);
        if (targetUserId != null) {
            authorizationService.preventSelfAction(targetUserId, preventSelfDeletion.message());
            log.debug("Self-deletion prevention check passed");
        }
    }

    @Before("@annotation(selfAccessOnly)")
    public void checkSelfAccessOnly(JoinPoint joinPoint, SelfAccessOnly selfAccessOnly) {
        Long targetUserId = AspectUtils.extractParameterValue(joinPoint, selfAccessOnly.userIdParam(), Long.class);
        if (targetUserId != null) {
            if (!authorizationService.isSameUser(targetUserId)) {
                throw new com.raffleease.raffleease.Common.Exceptions.CustomExceptions.AuthorizationException(
                    selfAccessOnly.message()
                );
            }
            log.debug("Self-access-only authorization passed");
        }
    }
} 