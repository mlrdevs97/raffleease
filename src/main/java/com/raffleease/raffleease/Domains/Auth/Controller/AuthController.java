package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.Register.RegisterRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.LoginRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.RegisterEmailVerificationRequest;
import com.raffleease.raffleease.Domains.Auth.Services.AuthValidationService;
import com.raffleease.raffleease.Domains.Auth.Services.LoginService;
import com.raffleease.raffleease.Domains.Auth.Services.RegisterService;
import com.raffleease.raffleease.Domains.Auth.Services.VerificationService;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {
    private final RegisterService registerService;
    private final LoginService loginService;
    private final LogoutHandler logoutHandler;
    private final AuthValidationService authValidationService;
    private final VerificationService verificationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        registerService.register(request, response);
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        null,
                        "New association account created successfully"
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verify(
            @RequestBody @Valid RegisterEmailVerificationRequest request
    ) {
        verificationService.verifyEmail(request.verificationToken());
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        null,
                        "Account verified successfully"
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        loginService.login(request, response),
                        "User authenticated successfully"
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse> validate() {
        authValidationService.isUserAuthenticated();
        return ResponseEntity.ok().body(
                ResponseFactory.success("User authentication validated successfully")
        );
    }
}
