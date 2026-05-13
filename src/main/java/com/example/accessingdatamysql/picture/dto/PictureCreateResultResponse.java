package com.example.accessingdatamysql.picture.dto;

import com.example.accessingdatamysql.gamification.dto.GamificationUpdateResponse;
import com.example.accessingdatamysql.picture.dto.PictureResponse;

public class PictureCreateResultResponse {
    private PictureResponse picture;
    private GamificationUpdateResponse gamification;

    public PictureCreateResultResponse(
            PictureResponse picture,
            GamificationUpdateResponse gamification
    ) {
        this.picture = picture;
        this.gamification = gamification;
    }

    public PictureResponse getPicture() { return picture; }
    public GamificationUpdateResponse getGamification() { return gamification; }
}