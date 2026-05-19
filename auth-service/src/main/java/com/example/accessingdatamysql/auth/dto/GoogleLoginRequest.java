package com.example.accessingdatamysql.auth.dto;

/**
 * DataTransferObject which is used as request-body for Google-login
 *
 * <p>Contains the Google ID-token which is sent from the client to
 * backend for authentication, where the token is verified before user
 * is logged in or created.</p>
 */
public class GoogleLoginRequest {

    private String token;

    public GoogleLoginRequest() {

    }

    public GoogleLoginRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
