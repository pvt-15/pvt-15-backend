package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class VisionService {
    private static final String IMAGE_URL_REQUIRED = "Image URL is required";
    private static final String VISION_API_KEY_MISSING = "Vision API key is missing";
    private static final String VISION_ENDPOINT = "https://vision.googleapis.com/v1/images:annotate?key=";
    private static final String VISION_API_REQUEST_FAILED = "Vision API request failed";
    private static final String VISION_API_ERROR = "Vision API returned an error: ";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String visionApiKey;

    public VisionService(@Value("${vision.api.key:}") String visionApiKey,
                         ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.visionApiKey = visionApiKey;
        this.objectMapper = objectMapper;
    }

    public AiIdentificationResult identifyImage(String imageUrl, TargetType targetType) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        if (visionApiKey == null || visionApiKey.isBlank()) {
            throw new IllegalStateException(VISION_API_KEY_MISSING);
        }

        TargetType effectiveTarget = targetType == null ? TargetType.ANIMAL : targetType;

        try {
            String requestBody = buildRequestBody(imageUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VISION_ENDPOINT + visionApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        VISION_API_REQUEST_FAILED + " with status " + response.statusCode() + ": " + response.body()
                );
            }

            return parseVisionResponse(response.body(), effectiveTarget);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(VISION_API_REQUEST_FAILED, e);
        } catch (IOException e) {
            throw new IllegalStateException(VISION_API_REQUEST_FAILED, e);
        }
    }

    private String buildRequestBody(String imageUrl) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode requests = root.putArray("requests");
        ObjectNode request = requests.addObject();

        request.putObject("image")
                .putObject("source")
                .put("imageUri", imageUrl);

        ArrayNode features = request.putArray("features");
        features.addObject()
                .put("type", "LABEL_DETECTION")
                .put("maxResults", 10);

        return objectMapper.writeValueAsString(root);
    }

    private AiIdentificationResult parseVisionResponse(String responseBody, TargetType targetType) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode errorNode = root.path("responses").path(0).path("error");

        if (!errorNode.isMissingNode() && !errorNode.isEmpty()) {
            throw new IllegalStateException(VISION_API_ERROR + errorNode);
        }

        JsonNode labels = root.path("responses").path(0).path("labelAnnotations");
        if (!labels.isArray() || labels.isEmpty()) {
            return unknownResult(targetType);
        }

        List<VisionLabel> visionLabels = new ArrayList<>();
        for (JsonNode labelNode : labels) {
            String description = labelNode.path("description").asText("").trim();
            double score = labelNode.path("score").asDouble(0.0);
            if (!description.isBlank()) {
                visionLabels.add(new VisionLabel(description, score));
            }
        }

        if (visionLabels.isEmpty()) {
            return unknownResult(targetType);
        }

        VisionLabel selectedLabel = chooseBestLabel(visionLabels, targetType);
        return new AiIdentificationResult(
                normalizeLabel(selectedLabel.description()),
                mapCategory(selectedLabel.description(), targetType),
                selectedLabel.score()
        );
    }

    private AiIdentificationResult unknownResult(TargetType targetType) {
        if (targetType == TargetType.PLANT) {
            return new AiIdentificationResult("Unknown Plant", "UNKNOWN", 0.0);
        }
        return new AiIdentificationResult("Unknown Animal", "UNKNOWN", 0.0);
    }

    private VisionLabel chooseBestLabel(List<VisionLabel> labels, TargetType targetType) {
        return labels.stream()
                .filter(label -> !isNoiseLabel(label.description(), targetType))
                .max(Comparator.comparingDouble(label -> scoreLabel(label.description(), label.score(), targetType)))
                .orElseGet(() -> labels.stream()
                        .max(Comparator.comparingDouble(VisionLabel::score))
                        .orElse(new VisionLabel(targetType == TargetType.PLANT ? "Unknown Plant" : "Unknown Animal", 0.0)));
    }

    private double scoreLabel(String label, double baseScore, TargetType targetType) {
        String normalized = normalizeKeyword(label);
        double score = baseScore;

        if (targetType == TargetType.ANIMAL) {
            if (isSpecificBird(normalized)) {
                score += 0.30;
            } else if (isGenericBird(normalized)) {
                score += 0.20;
            }

            if (isSpecificInsect(normalized)) {
                score += 0.25;
            } else if (containsAny(normalized, "insect")) {
                score += 0.15;
            }

            if (isSpecificAnimal(normalized)) {
                score += 0.20;
            } else if (containsAny(normalized, "animal", "mammal", "wildlife", "fauna")) {
                score += 0.05;
            }
        } else {
            if (isSpecificTree(normalized)) {
                score += 0.30;
            } else if (containsAny(normalized, "tree", "conifer", "deciduous", "woody plant")) {
                score += 0.18;
            }

            if (isSpecificFlower(normalized)) {
                score += 0.28;
            } else if (containsAny(normalized, "flower", "blossom", "petal", "wildflower")) {
                score += 0.15;
            }

            if (containsAny(normalized, "plant", "flora", "shrub", "bush", "fern", "moss", "herb")) {
                score += 0.08;
            }
        }

        if (containsAny(normalized, "close up", "macro photography", "terrestrial plant")) {
            score -= 0.03;
        }

        return score;
    }

    private String mapCategory(String label, TargetType targetType) {
        return targetType == TargetType.PLANT ? mapPlantCategory(label) : mapAnimalCategory(label);
    }

    private String mapAnimalCategory(String label) {
        String normalized = normalizeKeyword(label);

        if (isSpecificBird(normalized) || isGenericBird(normalized)) {
            return "BIRD";
        }
        if (isSpecificInsect(normalized) || containsAny(normalized,
                "insect", "bee", "bumblebee", "butterfly", "moth", "ant", "beetle", "dragonfly", "ladybug", "ladybird", "grasshopper", "wasp")) {
            return "INSECT";
        }
        if (containsAny(normalized,
                "animal", "mammal", "wildlife", "fauna", "fox", "wolf", "deer", "moose", "elk", "roe deer",
                "squirrel", "rabbit", "hare", "hedgehog", "badger", "boar", "otter", "seal", "dog", "cat",
                "horse", "cow", "sheep", "goat", "frog", "toad", "snake", "lizard", "fish", "spider")) {
            return "ANIMAL";
        }
        return "UNKNOWN";
    }

    private String mapPlantCategory(String label) {
        String normalized = normalizeKeyword(label);

        if (isSpecificTree(normalized) || containsAny(normalized,
                "tree", "conifer", "evergreen", "deciduous", "woody plant", "forest tree", "sapling")) {
            return "TREE";
        }
        if (isSpecificFlower(normalized) || containsAny(normalized,
                "flower", "wildflower", "blossom", "petal", "bloom", "inflorescence")) {
            return "FLOWER";
        }
        if (containsAny(normalized,
                "plant", "flora", "leaf", "branch", "foliage", "shrub", "bush", "fern", "moss", "grass", "herb")) {
            return "PLANT";
        }
        return "UNKNOWN";
    }

    private boolean isNoiseLabel(String label, TargetType targetType) {
        String normalized = normalizeKeyword(label);

        if (targetType == TargetType.ANIMAL) {
            return containsAny(normalized,
                    "green", "grass", "field", "meadow", "pasture", "plant", "leaf", "tree", "landscape",
                    "vegetation", "nature reserve", "outdoor", "sky", "branch", "woodland");
        }

        return containsAny(normalized,
                "sky", "cloud", "landscape", "outdoor", "photography", "close up", "macro photography",
                "organism", "natural environment");
    }

    private boolean isGenericBird(String normalized) {
        return containsAny(normalized, "bird", "avian");
    }

    private boolean isSpecificBird(String normalized) {
        return containsAny(normalized,
                "duck", "swan", "goose", "gull", "pigeon", "dove", "crow", "raven", "magpie", "owl",
                "eagle", "hawk", "falcon", "sparrow", "robin", "woodpecker", "heron", "crane", "stork",
                "tit", "finch", "blackbird", "jay", "wren", "kingfisher");
    }

    private boolean isSpecificInsect(String normalized) {
        return containsAny(normalized,
                "butterfly", "moth", "bee", "bumblebee", "wasp", "ant", "beetle", "dragonfly",
                "damselfly", "ladybug", "ladybird", "grasshopper", "caterpillar");
    }

    private boolean isSpecificAnimal(String normalized) {
        return containsAny(normalized,
                "fox", "wolf", "dog", "cat", "deer", "moose", "elk", "roe deer", "squirrel", "rabbit",
                "hare", "hedgehog", "badger", "boar", "otter", "seal", "horse", "cow", "sheep", "goat",
                "frog", "toad", "snake", "lizard", "fish", "spider", "rodent", "reptile", "amphibian");
    }

    private boolean isSpecificTree(String normalized) {
        return containsAny(normalized,
                "pine", "spruce", "fir", "birch", "oak", "maple", "ash", "alder", "willow", "rowan",
                "linden", "lime tree", "hazel", "juniper", "cedar", "cypress", "elm", "beech");
    }

    private boolean isSpecificFlower(String normalized) {
        return containsAny(normalized,
                "rose", "tulip", "daisy", "dandelion", "sunflower", "lily", "orchid", "violet", "anemone",
                "crocus", "buttercup", "poppy", "bluebell", "snowdrop", "lavender");
    }

    private String normalizeLabel(String label) {
        if (label == null || label.isBlank()) {
            return "Unknown Species";
        }
        return label.trim();
    }

    private String normalizeKeyword(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace('_', ' ');
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private record VisionLabel(String description, double score) {
    }
}
