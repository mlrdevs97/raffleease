package com.raffleease.raffleease.Domains.Users.Controller;

import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import com.raffleease.raffleease.Domains.Auth.DTOs.EditPasswordRequest;
import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Auth.Validations.AdminOnly;
import com.raffleease.raffleease.Domains.Auth.Validations.RequireRole;
import com.raffleease.raffleease.Domains.Auth.Validations.PreventSelfDeletion;
import com.raffleease.raffleease.Domains.Auth.Validations.SelfAccessOnly;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.EditUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Services.UsersManagementService;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.raffleease.raffleease.Domains.Associations.Model.AssociationRole.ADMIN;
import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RequestMapping("/v1/associations/{associationId}/users")
@RestController
@ValidateAssociationAccess
public class UsersController {
    private final UsersManagementService usersManagementService;
    private final UsersService usersService;

    @PostMapping
    @AdminOnly(message = "Only administrators can create user accounts")
    public ResponseEntity<ApiResponse> create(
            @PathVariable Long associationId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse userResponse = usersManagementService.create(
                associationId, 
                request
        );
        return ResponseEntity.status(CREATED).body(
                ResponseFactory.success(
                        userResponse,
                        "User account created successfully. Verification email sent."
                )
        );
    }

    @GetMapping
    @AdminOnly(message = "Only administrators can access user accounts information")
    public ResponseEntity<ApiResponse> getAll(
            @PathVariable Long associationId
    ) {
        List<UserResponse> users = usersService.getUsersByAssociationId(associationId);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        users,
                        "Users retrieved successfully"
                )
        );
    }

    @GetMapping("/{userId}")
    @RequireRole(
        value = ADMIN,
        allowSelfAccess = true,
        message = "Only administrators can access other users' account information, or users can access their own account"
    )
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long associationId,
            @PathVariable Long userId
    ) {
        UserResponse user = usersService.getUserById(userId);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        user,
                        "User retrieved successfully"
                )
        );
    }

    @PutMapping("/{userId}")
    @RequireRole(
        value = ADMIN,
        allowSelfAccess = true,
        message = "Only administrators can update user accounts, or users can update their own account"
    )
    public ResponseEntity<ApiResponse> edit(
            @PathVariable Long associationId,
            @PathVariable Long userId,
            @Valid @RequestBody EditUserRequest request
    ) {
        UserResponse userResponse = usersManagementService.edit(
                associationId, 
                userId,
                request.userData()
        );
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        userResponse,
                        "User updated successfully"
                )
        );
    }

    @PutMapping("/{userId}/password")
    @SelfAccessOnly(message = "You can only change your own password")
    public ResponseEntity<ApiResponse> editPassword(
            @PathVariable Long associationId,
            @PathVariable Long userId,
            @Valid @RequestBody EditPasswordRequest request
    ) {
        usersManagementService.editPassword(request);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        null,
                        "Password has been updated successfully"
                )
        );
    }

    @PatchMapping("/{userId}/disable")
    @AdminOnly(message = "Only administrators can disable user accounts")
    @PreventSelfDeletion(message = "Administrators cannot disable their own account")
    public ResponseEntity<ApiResponse> disableUser(
            @PathVariable Long associationId,
            @PathVariable Long userId
    ) {
        usersManagementService.disableUserInAssociation(associationId, userId);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        null,
                        "User disabled successfully"
                )
        );
    }

    @PatchMapping("/{userId}/enable")
    @AdminOnly(message = "Only administrators can enable user accounts")
    public ResponseEntity<ApiResponse> enableUser(
            @PathVariable Long associationId,
            @PathVariable Long userId
    ) {
        usersManagementService.enableUserInAssociation(associationId, userId);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        null,
                        "User enabled successfully"
                )
        );
    }
} 