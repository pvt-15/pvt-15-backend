package com.example.accessingdatamysql.auth.client;

public class AuthRevocationStatusResponse {

    private boolean revoked;

    public AuthRevocationStatusResponse() {
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}