package com.example.accessingdatamysql.dto;

public class GoogleUserInfo {

    private String providerUserId;
    private String email;
    private String name;

    public GoogleUserInfo(String providerUserId, String email, String name){
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
