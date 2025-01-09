package com.raffleease.raffleease.Domains.Auth.Controller;

import com.raffleease.raffleease.Domains.Auth.DTOs.AuthRequest;
import com.raffleease.raffleease.Domains.Auth.Services.IAuthValidationService;
import com.raffleease.raffleease.Domains.Auth.Services.ILoginService;
import com.raffleease.raffleease.Domains.Auth.Services.IRegisterService;
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
    private final IRegisterService registerService;
    private final ILoginService loginService;
    private final LogoutHandler logoutHandler;
    private final IAuthValidationService authValidationService;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok().body(
                ResponseFactory.success(
                        loginService.authenticate(request, response),
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
        this.authValidationService.isUserAuthenticated();
        return ResponseEntity.ok().body(
                ResponseFactory.success("User authentication validated successfully")
        );
    }
}
