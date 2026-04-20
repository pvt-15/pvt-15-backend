package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


@Service
public class NatureAiService{

    private static final String IMAGE_URL_REQUIRED = "Image URL is required";
    private static final String VISION_API_KEY_MISSING = "Vision API key is missing";
    private static final String VISION_ENDPOINT = "https://vision.googleapis.com/v1/images:annotate?key=";
    private static final String VISION_API_REQUEST_FAILED = "Vision API request failed";
    private static final String VISION_API_ERROR = "Vision API returned an error: ";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String visionApiKey;

    public NatureAiService(@Value("${vision.api.key}") String visionApiKey,
                           ObjectMapper objectMapper){
        this.httpClient = HttpClient.newHttpClient();
        this.visionApiKey = visionApiKey;
        this.objectMapper = objectMapper;
    }

    public AiIdentificationResult identifyImage(String imageUrl){
        if(imageUrl == null || imageUrl.isBlank()){
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        if(visionApiKey == null || visionApiKey.isBlank()){
            throw new IllegalStateException(VISION_API_KEY_MISSING);
        }

        try{
            String requestBody = buildRequestBody(imageUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VISION_ENDPOINT + visionApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if(response.statusCode() < 200 || response.statusCode() >= 300){
                throw new IllegalStateException(VISION_API_REQUEST_FAILED + " with status " + response.statusCode()
                + ": " + response.body());
            }
            return parseVisionResponse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(VISION_API_REQUEST_FAILED, e);
        } catch (IOException e) {
            throw new IllegalStateException(VISION_API_REQUEST_FAILED, e);
        }
    }

    private String buildRequestBody(String imageUrl) throws IOException{
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode requests = root.putArray("requests");

        ObjectNode request = requests.addObject();
        request.putObject("image").putObject("source")
                .put("imageUri", imageUrl);

        ArrayNode features = request.putArray("features");
        features.addObject().put("type", "LABEL_DETECTION")
                .put("maxResults", 10);

        return objectMapper.writeValueAsString(root);
    }

    private AiIdentificationResult parseVisionResponse(String responseBody) throws IOException{
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode errorNode = root.path("responses").path(0).path("error");
        if(!errorNode.isMissingNode() && !errorNode.isEmpty()){
            throw new IllegalStateException(VISION_API_ERROR + errorNode.toString());
        }

        JsonNode labels = root.path("responses").path(0).path("labelAnnotations");

        if(!labels.isArray() || labels.isEmpty()){
            return new AiIdentificationResult("Unknown Species", "UNKNOWN", 0.0);
        }

        List<VisionLabel> visionLabels = new ArrayList<>();

        for(JsonNode labelNode : labels){
            String description = labelNode.path("description").asText("").trim();
            double score = labelNode.path("score").asDouble(0.0);

            if(!description.isBlank()){
                System.out.println("Vision label: " + description + " score: " + score);
                visionLabels.add(new VisionLabel(description, score));
            }
        }

        if(visionLabels.isEmpty()){
            return new AiIdentificationResult("Unknown Species", "UNKNOWN", 0.0);
        }

        VisionLabel selectedLabel = chooseBestRelevantLabel(visionLabels);
        String category = mapCategory(visionLabels, selectedLabel);

        return new AiIdentificationResult(
                normalizeLabel(selectedLabel.description()),
                category,
                selectedLabel.score()
        );
    }

    private String normalizeLabel(String label){
        if(label == null || label.isBlank()){
            return  "Unknown Species";
        }
        return label.trim();
    }

    private String mapCategory(List<VisionLabel> labels, VisionLabel selectedLabel) {
        DetectedCategory selectedCategory = classifyLabel(selectedLabel.description());

        if(selectedCategory != DetectedCategory.UNKNOWN){
            return selectedCategory.name();
        }

        Map<DetectedCategory, Double> categoryScores = new EnumMap<>(DetectedCategory.class);
        for(DetectedCategory category : DetectedCategory.values()){
            categoryScores.put(category, 0.0);
        }

        for(VisionLabel label : labels){
            String normalized = normalizeKeyword(label.description());
            DetectedCategory category = classifyLabel(normalized);

            if(category == DetectedCategory.UNKNOWN){
                continue;
            }

            double weight = label.score;

            if(isSpecificLabel(normalized)){
                weight *= 1.35;
            }else{
                weight *= 0.85;
            }

            switch(category){
                case ANIMAL, BIRD, INSECT, FLOWER -> weight *= 1.20;
                case TREE -> weight *= 1.00;
                case PLANT -> weight *= 0.70;
                default -> { }
            }
            categoryScores.put(category, categoryScores.get(category) + weight);
        }
        return categoryScores.entrySet().stream()
                .filter(entry -> entry.getKey() != DetectedCategory.UNKNOWN)
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().name())
                .orElse("UNKNOWN");
    }

    private VisionLabel chooseBestRelevantLabel(List<VisionLabel> labels){
        VisionLabel bestSpecific = null;
        double bestSpecificScore = -1.0;

        VisionLabel bestInterestingGeneric = null;
        double bestInterestingGenericScore = -1.0;

        for(VisionLabel label : labels){
            String normalized = normalizeKeywoard(label.description());

            if(isNoiseLabel(normalized)){
                continue;
            }
        }
    }

    private record VisionLabel(String description, double score){

    }

    private enum DetectedCategory{
        ANIMAL,
        BIRD,
        INSECT,
        FLOWER,
        TREE,
        PLANT,
        UNKNOWN
    }
}