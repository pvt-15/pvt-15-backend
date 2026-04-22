package com.example.accessingdatamysql.auth.dto;

/**
 * DataTransferObject which is used as request-body for local login.
 *
 * <p>Contains the necessary credentials to authorize a user through
 * the systems normal email/password-based login.</p>
 */
public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest() {

    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
