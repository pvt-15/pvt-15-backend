package com.example.accessingdatamysql.auth.dto;

/**
 * Request body for {@code POST /auth/forgot-password}.
 *
 * <p>Carries the email address the user wants a reset link sent to.</p>
 */
public class ForgotPasswordRequest {

    private String email;

    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}