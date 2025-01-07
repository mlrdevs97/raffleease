package com.raffleease.raffleease.Domains.Payments.Controller;

import com.raffleease.raffleease.Domains.Payments.Services.IStripeService;
import com.raffleease.raffleease.Domains.Payments.Services.IWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stripe")
public class StripeController {
    private final IStripeService stripeService;
    private final IWebhookService webHookService;

    @GetMapping("/keys/public")
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(stripeService.getPublicKey());
    }

    @PostMapping("/webhooks")
    public ResponseEntity<Void> handleWebHook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        webHookService.handleWebHook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}