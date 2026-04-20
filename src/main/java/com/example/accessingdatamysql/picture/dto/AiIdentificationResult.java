package com.example.accessingdatamysql.picture.dto;

public class AiIdentificationResult {

    private String label;
    private String category;
    private double aiConfidence;

    public AiIdentificationResult(){

    }

    public AiIdentificationResult(String label,
                                  String category,
                                  double aiConfidence){
        this.label = label;
        this.category = category;
        this.aiConfidence = aiConfidence;
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

    public void setLabel(String label) {
        this.label = label;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAiConfidence(double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }
}
