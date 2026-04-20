package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;

public class NatureAiService {
    public CreatePictureRequest identify(){
        CreatePictureRequest request = new CreatePictureRequest();
        request.setLabel("Daisy");
        request.setCategory("FLOWER");
        request.setAiConfidence(0.87);
        request.setImageUrl(null);
        return request;
    }
}
