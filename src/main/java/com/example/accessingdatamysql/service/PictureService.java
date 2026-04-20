package com.example.accessingdatamysql.service;

import com.example.accessingdatamysql.dto.CreatePictureRequest;
import com.example.accessingdatamysql.dto.PictureResponse;
import com.example.accessingdatamysql.model.Picture;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import com.example.accessingdatamysql.repository.PictureRepository;
import com.example.accessingdatamysql.repository.UserRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PictureService {

    private static final String USER_NOT_FOUND = "User not found";

    private final PictureRepository pictureRepository;
    private final UserRepository userRepository;

    public PictureService(PictureRepository pictureRepository,
                          UserRepository userRepository){
        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
    }

    public PictureResponse createPicture(Integer userId,
                                         CreatePictureRequest request){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        Picture picture = new Picture();
        picture.setLabel(request.getLabel());
        picture.setCategory(parseCategory(request.getCategory()));
        picture.setAiConfidence(request.getAiConfidence());
        picture.setImageUrl(request.getImageUrl());
        picture.setTakenAt(LocalDateTime.now());
        picture.setUser(user);

        //TODO CHANGE CALCULATION OF POINTS
        int points = calculatePoints(request.getAiConfidence());
        picture.setPointsAwarded(points);

        //TODO CHANGE HOW WE CALCULATE LEVEL
        user.setTotalPoints(user.getTotalPoints() + points);
        user.setLevel(calculateLevel(user.getTotalPoints()));

        Picture saved = pictureRepository.save(picture);

        return toResponse(saved);
    }

    public List<PictureResponse> getMyPictures(Integer userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        List<PictureResponse> responses = new ArrayList<>();
        for(Picture picture : pictureRepository.findByUser(user)){
            responses.add(toResponse(picture));
        }

        return responses;
    }

    private int calculatePoints(double confidence){
        if(confidence >= 0.90)
            return 20;
        if(confidence >= 0.75)
            return 15;
        if(confidence >= 0.60)
            return 10;
        return 0;
    }

    private Level calculateLevel(int totalPoints){
        if(totalPoints >= 300)
            return Level.LEVEL_3;
        if(totalPoints >= 150)
            return Level.LEVEL_2;
        return Level.LEVEL_1;
    }

    private PictureCategory parseCategory(String category){
        try{
            return PictureCategory.valueOf(category.toUpperCase());
        }catch(Exception e){
            return PictureCategory.UNKNOWN;
        }
    }

    private PictureResponse toResponse(Picture observation) {
        return new PictureResponse(
                observation.getId(),
                observation.getLabel(),
                observation.getCategory().name(),
                observation.getAiConfidence(),
                observation.getPointsAwarded(),
                observation.getImageUrl(),
                observation.getTakenAt().toString()
        );
    }
}
