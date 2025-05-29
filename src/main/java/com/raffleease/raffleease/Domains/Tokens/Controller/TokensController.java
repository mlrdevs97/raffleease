package com.raffleease.raffleease.Domains.Tokens.Controller;

import com.raffleease.raffleease.Domains.Tokens.Services.TokensManagementService;
import com.raffleease.raffleease.Common.Responses.ApiResponse;
import com.raffleease.raffleease.Common.Responses.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/tokens")
public class TokensController {
    private final TokensManagementService service;

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
}
