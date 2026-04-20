package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
public class NatureAiService{

    private static final String IMAGE_URL_REQUIRED = "Image URL is required";
    private static final String VISION_API_KEY_MISSING = "Vision API key is missing";
    private static final String VISION_ENDPOINT = "https://vision.googleapis.com/v1/images:annotate?key=";
    private static final String VISION_API_REQUEST_FAILED = "Vision API request failed";

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
        JsonNode labels = root.path("responses").path(0).path("labelAnnotations");

        if(!labels.isArray() || labels.isEmpty()){
            return new AiIdentificationResult("Unknown Species", "UNKNOWN", 0.0);
        }

        String bestLabel = "Unknown Species";
        double bestScore = 0.0;
        List<String> allLabels = new ArrayList<>();

        for(JsonNode labelNode : labels){
            String description = labelNode.path("description").asText("");
            double score = labelNode.path("score").asDouble(0.0);

            System.out.println("Vision label: " + description + " score: " + score);

            if(!description.isBlank()){
                allLabels.add(description);
            }

            if(score > bestScore){
                bestScore = score;
                bestLabel = description;
            }
        }
        String category = mapCategory(allLabels, bestLabel);

        return new AiIdentificationResult(
                normalizeLabel(bestLabel),
                category,
                bestScore
        );
    }

    private String normalizeLabel(String label){
        if(label == null || label.isBlank()){
            return  "Unknown Species";
        }
        return label.trim();
    }

    private String mapCategory(List<String> labels, String bestLabel) {
        String best = bestLabel == null ? "" : bestLabel.toLowerCase(Locale.ROOT);

        // 1. Prioritera huvudlabeln först
        if (containsAny(best, "bird", "sparrow", "eagle", "owl", "duck", "parrot", "pigeon")) {
            return "BIRD";
        }

        if (containsAny(best, "insect", "butterfly", "bee", "beetle", "ant", "dragonfly", "spider")) {
            return "INSECT";
        }

        if (containsAny(best, "flower", "rose", "daisy", "sunflower", "tulip", "blossom")) {
            return "FLOWER";
        }

        if (containsAny(best, "tree", "oak", "pine", "maple", "birch")) {
            return "TREE";
        }

        if (containsAny(best, "animal", "dog", "cat", "fox", "deer", "rabbit", "squirrel", "mammal", "horse", "cow")) {
            return "ANIMAL";
        }

        if (containsAny(best, "plant", "leaf", "grass", "fern", "shrub", "herb")) {
            return "PLANT";
        }

        // 2. Om huvudlabeln inte räcker, titta på resten av labels
        String allText = String.join(" ", labels).toLowerCase(Locale.ROOT);

        if (containsAny(allText, "bird", "sparrow", "eagle", "owl", "duck", "parrot", "pigeon")) {
            return "BIRD";
        }

        if (containsAny(allText, "insect", "butterfly", "bee", "beetle", "ant", "dragonfly", "spider")) {
            return "INSECT";
        }

        if (containsAny(allText, "flower", "rose", "daisy", "sunflower", "tulip", "blossom")) {
            return "FLOWER";
        }

        if (containsAny(allText, "tree", "oak", "pine", "maple", "birch")) {
            return "TREE";
        }

        if (containsAny(allText, "animal", "dog", "cat", "fox", "deer", "rabbit", "squirrel", "mammal", "horse", "cow")) {
            return "ANIMAL";
        }

        if (containsAny(allText, "plant", "leaf", "grass", "fern", "shrub", "herb")) {
            return "PLANT";
        }

        return "UNKNOWN";
    }

    private boolean containsAny(String text, String... candidates){
        for(String candidate : candidates){
            if(text.contains((candidate.toLowerCase(Locale.ROOT)))){
                return true;
            }
        }
        return false;
    }
}