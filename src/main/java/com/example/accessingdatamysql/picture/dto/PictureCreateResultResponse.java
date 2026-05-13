package com.example.accessingdatamysql.picture.dto;

import com.example.accessingdatamysql.gamification.dto.GamificationUpdateResponse;
import com.example.accessingdatamysql.picture.enums.PictureRejectionReason;

public class PictureCreateResultResponse {

    private boolean accepted;
    private String message;
    private PictureRejectionReason rejectionReason;
    private PictureResponse picture;
    private GamificationUpdateResponse gamification;

    public PictureCreateResultResponse(boolean accepted,
                                       String message,
                                       PictureRejectionReason rejectionReason,
                                       PictureResponse picture,
                                       GamificationUpdateResponse gamification) {
        this.accepted = accepted;
        this.message = message;
        this.rejectionReason = rejectionReason;
        this.picture = picture;
        this.gamification = gamification;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getMessage() {
        return message;
    }

    public PictureRejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public PictureResponse getPicture() {
        return picture;
    }

    public GamificationUpdateResponse getGamification() {
        return gamification;
    }

    public static PictureCreateResultResponse accepted(PictureResponse picture,
                                                       GamificationUpdateResponse gamification) {
        return new PictureCreateResultResponse(true, null, null, picture, gamification);
    }

    public static PictureCreateResultResponse rejected(PictureRejectionReason reason,
                                                       String message) {
        return new PictureCreateResultResponse(false, message, reason, null, null);
    }
}