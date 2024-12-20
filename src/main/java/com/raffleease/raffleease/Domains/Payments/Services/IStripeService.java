package com.raffleease.raffleease.Domains.Payments.Services;

import com.raffleease.raffleease.Domains.Payments.DTOs.SessionCreate;

public interface IStripeService {
    String getPublicKey();

    String createSession(SessionCreate request);
}
