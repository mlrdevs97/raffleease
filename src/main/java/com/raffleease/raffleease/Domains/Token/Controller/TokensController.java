package com.raffleease.raffleease.Domains.Token.Controller;

import com.raffleease.raffleease.Domains.Token.Services.ITokensManagementService;
import com.raffleease.raffleease.Responses.ApiResponse;
import com.raffleease.raffleease.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/token")
public class TokensController {
    private final ITokensManagementService service;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(ResponseFactory.success(
                service.refresh(request, response),
                "Token refreshed successfully"
        ));
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<Void> revoke(@RequestParam String token) {
        service.revokeAllUserTokens(token);
        return ResponseEntity.noContent().build();
    }
}
