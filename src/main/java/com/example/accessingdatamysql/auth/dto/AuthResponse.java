package com.example.accessingdatamysql.auth.dto;

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
