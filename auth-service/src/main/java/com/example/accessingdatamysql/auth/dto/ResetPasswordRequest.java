package com.example.accessingdatamysql.auth.dto;

/**
 * Request body for {@code POST /auth/reset-password}.
 *
 * <p>Carries the raw reset token (from the emailed link) and the new password
 * the user wants to set.</p>
 */
public class ResetPasswordRequest {

    private String token;
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}