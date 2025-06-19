package com.raffleease.raffleease.Domains.Users.Controller;

import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import com.raffleease.raffleease.Domains.Auth.Validations.ValidateAssociationAccess;
import com.raffleease.raffleease.Domains.Users.DTOs.CreateUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.EditUserRequest;
import com.raffleease.raffleease.Domains.Users.DTOs.UserResponse;
import com.raffleease.raffleease.Domains.Users.Services.UserManagementService;
import com.raffleease.raffleease.Domains.Users.Services.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RequestMapping("/v1/associations/{associationId}/users")
@RestController
@ValidateAssociationAccess
public class UsersController {
    private final UserManagementService userManagementService;
    private final UsersService usersService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @PathVariable Long associationId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse userResponse = userManagementService.create(
                associationId, 
                request.userData()
        );
        return ResponseEntity.status(CREATED).body(
                ResponseFactory.success(
                        userResponse,
                        "User account created successfully. Verification email sent."
                )
        );
    }

    @GetMapping
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> get(
            @PathVariable Long associationId,
            @PathVariable Long id
    ) {
        UserResponse user = usersService.getUserById(id);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        user,
                        "User retrieved successfully"
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> edit(
            @PathVariable Long associationId,
            @PathVariable Long id,
            @Valid @RequestBody EditUserRequest request
    ) {
        UserResponse userResponse = userManagementService.edit(
                associationId, 
                id,
                request.userData()
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