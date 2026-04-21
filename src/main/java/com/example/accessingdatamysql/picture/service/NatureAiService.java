package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import org.springframework.stereotype.Service;

@Service
public class NatureAiService {

    private static final String IMAGE_URL_REQUIRED = "Image URL is required";

    private final VisionService visionService;
    private final PlantNetService plantNetService;

    public NatureAiService(VisionService visionService, PlantNetService plantNetService) {
        this.visionService = visionService;
        this.plantNetService = plantNetService;
    }

    public AiIdentificationResult identifyImage(String imageUrl, TargetType targetType) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        TargetType actualTargetType = targetType;

        if (actualTargetType == null) {
            actualTargetType = TargetType.ANIMAL;
        }
        if (actualTargetType == TargetType.PLANT) {
            return identifyPlant(imageUrl);
        }
        return visionService.identifyImage(imageUrl, TargetType.ANIMAL);
    }

    private AiIdentificationResult identifyPlant(String imageUrl) {
        try {
            AiIdentificationResult result = plantNetService.identifyPlant(imageUrl);
            if (isGoodPlantResult(result)) {
                return result;
            }
        } catch (Exception ignored) {
            // If PlantNet fails, we fall back to Vision.
        }
        return visionService.identifyImage(imageUrl, TargetType.PLANT);
    }

    private boolean isGoodPlantResult(AiIdentificationResult result) {
        if (result == null) {
            return false;
        }
        if (result.getLabel() == null || result.getLabel().isBlank()) {
            return false;
        }
        if (result.getCategory() == null || result.getCategory().isBlank()) {
            return false;
        }
        return !"UNKNOWN".equalsIgnoreCase(result.getCategory());
    }
}