package com.example.accessingdatamysql.auth.dto;

/**
 * DataTransferObject that represents verified userinfo from Google
 *
 * <p>Object of this class is created after a Google ID-token is verified.
 * Contains the Google unique userid for account, users email and name.</p>
 */
public class GoogleUserInfo {

    private String providerUserId;
    private String email;
    private String name;

    public GoogleUserInfo(String providerUserId, String email, String name) {
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
