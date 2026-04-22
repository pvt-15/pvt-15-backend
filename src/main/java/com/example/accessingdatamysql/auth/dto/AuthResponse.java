package com.example.accessingdatamysql.auth.dto;

/**
 * DataTransferObject which is returned after successful authentication
 *
 * <p>Used as an answer from backend for registration, local login, and
 * Google-login. Contains basic userdata, a status message and a JWT-token
 * which the client uses for later authenticated requests.</p>
 */
public class AuthResponse {

    private Integer userId;
    private String name;
    private String email;
    private String message;
    private String token;

    public AuthResponse() {

    }

    public AuthResponse(Integer userId,
                        String name,
                        String email,
                        String message,
                        String token) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.message = message;
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
