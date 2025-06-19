package com.raffleease.raffleease.Domains.Users.Controller;

import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UpdateUserData;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Services.UserManagementService;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/v1/associations/{associationId}/users")
@RestController
@ValidateAssociationAccess
public class UsersController {
    private final UserManagementService userManagementService;
    private final UsersService usersService;

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(
            @PathVariable Long associationId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse userResponse = userManagementService.createUserInAssociation(
                associationId, 
                request.userData()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseFactory.success(
                        userResponse,
                        "User account created successfully. Verification email sent."
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUsers(
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
    public ResponseEntity<ApiResponse> getUser(
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
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable Long associationId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserData userData
    ) {
        UserResponse userResponse = userManagementService.updateUserInAssociation(
                associationId, 
                userId, 
                userData
        );
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        userResponse,
                        "User updated successfully"
                )
        );
    }

    @PatchMapping("/{userId}/disable")
    public ResponseEntity<ApiResponse> disableUser(
            @PathVariable Long associationId,
            @PathVariable Long userId
    ) {
        userManagementService.disableUserInAssociation(associationId, userId);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        null,
                        "User disabled successfully"
                )
        );
    }

    @PatchMapping("/{userId}/enable")
    public ResponseEntity<ApiResponse> enableUser(
            @PathVariable Long associationId,
            @PathVariable Long userId
    ) {
        userManagementService.enableUserInAssociation(associationId, userId);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        null,
                        "User enabled successfully"
                )
        );
    }
} 