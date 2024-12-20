package com.raffleease.raffleease.Domains.Payments.Services;

public interface IWebhookService {
    void handleWebHook(String payload, String sigHeader);
}
