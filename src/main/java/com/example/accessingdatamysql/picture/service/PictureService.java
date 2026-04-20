package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.model.Picture;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PictureService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String REQUEST_BODY_REQUIRED = "Request body is required";
    private static final String IMAGE_URL_REQUIRED = "Image URL is required";

    private final PictureRepository pictureRepository;
    private final UserRepository userRepository;
    private final NatureAiService natureAiService;

    public PictureService(PictureRepository pictureRepository,
                          UserRepository userRepository,
                          NatureAiService natureAiService){
        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
        this.natureAiService = natureAiService;
    }

    @Transactional
    public PictureResponse createPicture(Integer userId,
                                         CreatePictureRequest request){
        if(request == null){
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }
        if(request.getImageUrl() == null || request.getImageUrl().isBlank()){
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        AiIdentificationResult aiResult = natureAiService.identifyImage(request.getImageUrl());

        Picture picture = new Picture();
        picture.setLabel(aiResult.getLabel());
        picture.setCategory(parseCategory(aiResult.getCategory()));
        picture.setAiConfidence(aiResult.getAiConfidence());
        picture.setImageUrl(request.getImageUrl());
        picture.setTakenAt(LocalDateTime.now());
        picture.setUser(user);

        //TODO CHANGE CALCULATION OF POINTS
        int points = calculatePoints(aiResult.getAiConfidence());
        picture.setPointsAwarded(points);

        //TODO CHANGE HOW WE CALCULATE LEVEL
        user.setTotalPoints(user.getTotalPoints() + points);
        user.setLevel(calculateLevel(user.getTotalPoints()));

        userRepository.save(user);
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
        if(category == null || category.isBlank()){
            return PictureCategory.UNKNOWN;
        }
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
