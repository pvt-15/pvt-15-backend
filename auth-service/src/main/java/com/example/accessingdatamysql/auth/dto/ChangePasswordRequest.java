package com.example.accessingdatamysql.auth.dto;

/**
 * DataTransferObject which is used as request-body for changing the
 * password of an authenticated local user.
 *
 * <p>Contains the user's current password (for verification) and the
 * desired new password.</p>
 */
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;

    public ChangePasswordRequest() {

    }

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}