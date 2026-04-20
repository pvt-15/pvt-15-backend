package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import org.springframework.stereotype.Service;


@Service
public class NatureAiService{

    private static final String IMAGE_URL_REQUIRED = "Image URL is required";

    public AiIdentificationResult identifyImage(String imageUrl){
        if(imageUrl == null || imageUrl.isBlank()){
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        return new AiIdentificationResult("dog", "ANIMAL", 0.75);
    }
}