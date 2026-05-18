package com.example.accessingdatamysql.picture.dto;

import com.example.accessingdatamysql.picture.enums.AiProvider;

public class AiIdentificationResult {

    private String label;
    private String category;
    private double aiConfidence;
    private AiProvider aiProvider;

    public AiIdentificationResult(String label,
                                  String category,
                                  double aiConfidence,
                                  AiProvider aiProvider) {
        this.label = label;
        this.category = category;
        this.aiConfidence = aiConfidence;
        this.aiProvider = aiProvider;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public double getAiConfidence() {
        return aiConfidence;
    }

    public AiProvider getAiProvider() {
        return aiProvider;
    }
}