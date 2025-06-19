package com.raffleease.raffleease.Domains.Auth.Services;

import com.raffleease.raffleease.Domains.Auth.DTOs.ForgotPasswordRequest;
import com.raffleease.raffleease.Domains.Auth.DTOs.ResetPasswordRequest;

public interface PasswordResetService {
    void requestPasswordReset(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
} 