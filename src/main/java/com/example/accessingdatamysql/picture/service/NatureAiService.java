package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import org.springframework.stereotype.Service;

@Service
public class NatureAiService{
    private static final String IMAGE_URL_REQUIRED = "Image URL is required";

    private final VisionService visionService;
    private final PlantNetService plantNetService;

    public NatureAiService(VisionService visionService,
                           PlantNetService plantNetService){
        this.visionService = visionService;
        this.plantNetService = plantNetService;
    }

    public AiIdentificationResult identifyImage(String imageUrl,
                                                TargetType targetType){
        if(imageUrl == null || imageUrl.isBlank()){
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }

        TargetType effectiveTarget = targetType == null ? TargetType.ANIMAL : targetType;

        if(effectiveTarget == TargetType.PLANT){
            return identifyPlant(imageUrl);
        }

        return visionService.identifyImage(imageUrl, TargetType.ANIMAL);
    }

    private AiIdentificationResult identifyPlant(String imageUrl){
        try{
            AiIdentificationResult plantNetResult = plantNetService.identifyPlant(imageUrl);
            if(isUsefulPlantResult(plantNetResult)){
                return plantNetResult;
            }
            System.out.println("PlantNet returned non-useful result, falling back to Vision. Result=" + plantNetResult.getLabel());
        } catch (Exception e) {
            System.out.println("PlantNet failed, falling back to Vision: " + e.getMessage());
            e.printStackTrace();
        }
        return visionService.identifyImage(imageUrl, TargetType.PLANT);
    }

    private boolean isUsefulPlantResult(AiIdentificationResult result){
        if(result == null){
            return false;
        }
        if(result.getLabel() == null || result.getLabel().isBlank()){
            return false;
        }
        if("UNKNOWN".equalsIgnoreCase(result.getCategory())){
            return false;
        }
        return result.getAiConfidence() >= 0.20;
    }
}