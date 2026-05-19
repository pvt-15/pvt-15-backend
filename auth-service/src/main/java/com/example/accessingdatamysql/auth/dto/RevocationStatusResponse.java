package com.example.accessingdatamysql.auth.dto;

public class RevocationStatusResponse {

    private boolean revoked;

    public RevocationStatusResponse() {
    }

    public RevocationStatusResponse(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}