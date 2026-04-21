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
        this.objectMapper = objectMapper;
        this.visionApiKey = visionApiKey;
    }

    public AiIdentificationResult identifyImage(String imageUrl, TargetType targetType) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        if (visionApiKey == null || visionApiKey.isBlank()) {
            throw new IllegalStateException(VISION_API_KEY_MISSING);
        }
        TargetType actualTargetType = targetType;
        if (actualTargetType == null) {
            actualTargetType = TargetType.ANIMAL;
        }
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
            return parseVisionResponse(response.body(), actualTargetType);
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

        request.putArray("features")
                .addObject()
                .put("type", "LABEL_DETECTION")
                .put("maxResults", 10);

        return objectMapper.writeValueAsString(root);
    }

    private AiIdentificationResult parseVisionResponse(String responseBody, TargetType targetType) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode firstResponse = root.path("responses").path(0);
        JsonNode errorNode = firstResponse.path("error");

        if (!errorNode.isMissingNode() && !errorNode.isEmpty()) {
            throw new IllegalStateException(VISION_API_ERROR + errorNode);
        }
        JsonNode labelAnnotations = firstResponse.path("labelAnnotations");
        if (!labelAnnotations.isArray() || labelAnnotations.isEmpty()) {
            return unknownResult(targetType);
        }
        List<VisionLabel> labels = new ArrayList<>();
        for (JsonNode labelNode : labelAnnotations) {
            String description = labelNode.path("description").asText("").trim();
            double score = labelNode.path("score").asDouble(0.0);

            if (!description.isBlank()) {
                labels.add(new VisionLabel(description, score));
            }
        }
        if (labels.isEmpty()) {
            return unknownResult(targetType);
        }
        if (targetType == TargetType.PLANT) {
            return choosePlantResult(labels);
        }
        return chooseAnimalResult(labels);
    }

    private AiIdentificationResult choosePlantResult(List<VisionLabel> labels) {
        for (VisionLabel label : labels) {
            String text = normalize(label.description());

            if (isPlantNoise(text)) {
                continue;
            }
            if (isSpecificTree(text)) {
                return new AiIdentificationResult(cleanLabel(label.description()), "TREE", label.score());
            }
            if (isSpecificFlower(text)) {
                return new AiIdentificationResult(cleanLabel(label.description()), "FLOWER", label.score());
            }
        }

        for (VisionLabel label : labels) {
            String text = normalize(label.description());

            if (isPlantNoise(text)) {
                continue;
            }
            String category = mapPlantCategory(text);
            if (!category.equals("UNKNOWN")) {
                return new AiIdentificationResult(cleanLabel(label.description()), category, label.score());
            }
        }

        for (VisionLabel label : labels) {
            String text = normalize(label.description());
            if (!isPlantNoise(text)) {
                return new AiIdentificationResult(cleanLabel(label.description()), "PLANT", label.score());
            }
        }
        return unknownResult(TargetType.PLANT);
    }

    private AiIdentificationResult chooseAnimalResult(List<VisionLabel> labels) {
        for (VisionLabel label : labels) {
            String text = normalize(label.description());

            if (isAnimalNoise(text)) {
                continue;
            }
            if (isSpecificBird(text)) {
                return new AiIdentificationResult(cleanLabel(label.description()), "BIRD", label.score());
            }
            if (isSpecificInsect(text)) {
                return new AiIdentificationResult(cleanLabel(label.description()), "INSECT", label.score());
            }
            if (isSpecificAnimal(text)) {
                return new AiIdentificationResult(cleanLabel(label.description()), "ANIMAL", label.score());
            }
        }

        for (VisionLabel label : labels) {
            String text = normalize(label.description());

            if (isAnimalNoise(text)) {
                continue;
            }
            String category = mapAnimalCategory(text);
            if (!category.equals("UNKNOWN")) {
                return new AiIdentificationResult(cleanLabel(label.description()), category, label.score());
            }
        }

        for (VisionLabel label : labels) {
            String text = normalize(label.description());
            if (!isAnimalNoise(text)) {
                return new AiIdentificationResult(cleanLabel(label.description()), "ANIMAL", label.score());
            }
        }

        return unknownResult(TargetType.ANIMAL);
    }

    private AiIdentificationResult unknownResult(TargetType targetType) {
        if (targetType == TargetType.PLANT) {
            return new AiIdentificationResult("Unknown Plant", "UNKNOWN", 0.0);
        }
        return new AiIdentificationResult("Unknown Animal", "UNKNOWN", 0.0);
    }

    private String mapPlantCategory(String text) {
        if (isSpecificTree(text) || containsAny(text,
                "tree", "conifer", "evergreen", "deciduous", "woody plant", "forest tree", "sapling")) {
            return "TREE";
        }
        if (isSpecificFlower(text) || containsAny(text,
                "flower", "wildflower", "blossom", "petal", "bloom", "inflorescence")) {
            return "FLOWER";
        }
        if (containsAny(text,
                "plant", "flora", "leaf", "branch", "foliage", "shrub", "bush", "fern", "moss", "grass", "herb")) {
            return "PLANT";
        }
        return "UNKNOWN";
    }

    private String mapAnimalCategory(String text) {
        if (isSpecificBird(text) || containsAny(text, "bird", "avian")) {
            return "BIRD";
        }
        if (isSpecificInsect(text) || containsAny(text,
                "insect", "bee", "bumblebee", "butterfly", "moth", "ant", "beetle",
                "dragonfly", "ladybug", "ladybird", "grasshopper", "wasp")) {
            return "INSECT";
        }
        if (isSpecificAnimal(text) || containsAny(text,
                "animal", "mammal", "wildlife", "fauna", "fox", "wolf", "deer", "moose",
                "elk", "roe deer", "squirrel", "rabbit", "hare", "hedgehog", "badger",
                "boar", "otter", "seal", "dog", "cat", "horse", "cow", "sheep", "goat",
                "frog", "toad", "snake", "lizard", "fish", "spider")) {
            return "ANIMAL";
        }
        return "UNKNOWN";
    }

    private boolean isPlantNoise(String text) {
        return containsAny(text,
                "sky", "cloud", "landscape", "outdoor", "photography", "close up",
                "macro photography", "organism", "natural environment");
    }

    private boolean isAnimalNoise(String text) {
        return containsAny(text,
                "green", "grass", "field", "meadow", "pasture", "plant", "leaf",
                "tree", "landscape", "vegetation", "nature reserve", "outdoor",
                "sky", "branch", "woodland");
    }

    private boolean isSpecificBird(String text) {
        return containsAny(text,
                "duck", "swan", "goose", "gull", "pigeon", "dove", "crow", "raven",
                "magpie", "owl", "eagle", "hawk", "falcon", "sparrow", "robin",
                "woodpecker", "heron", "crane", "stork", "tit", "finch", "blackbird",
                "jay", "wren", "kingfisher");
    }

    private boolean isSpecificInsect(String text) {
        return containsAny(text,
                "butterfly", "moth", "bee", "bumblebee", "wasp", "ant", "beetle",
                "dragonfly", "damselfly", "ladybug", "ladybird", "grasshopper", "caterpillar");
    }

    private boolean isSpecificAnimal(String text) {
        return containsAny(text,
                "fox", "wolf", "dog", "cat", "deer", "moose", "elk", "roe deer",
                "squirrel", "rabbit", "hare", "hedgehog", "badger", "boar", "otter",
                "seal", "horse", "cow", "sheep", "goat", "frog", "toad", "snake",
                "lizard", "fish", "spider", "rodent", "reptile", "amphibian");
    }

    private boolean isSpecificTree(String text) {
        return containsAny(text,
                "pine", "spruce", "fir", "birch", "oak", "maple", "ash", "alder",
                "willow", "rowan", "linden", "lime tree", "hazel", "juniper",
                "cedar", "cypress", "elm", "beech");
    }

    private boolean isSpecificFlower(String text) {
        return containsAny(text,
                "rose", "tulip", "daisy", "dandelion", "sunflower", "lily", "orchid",
                "violet", "anemone", "crocus", "buttercup", "poppy", "bluebell",
                "snowdrop", "lavender");
    }

    private String cleanLabel(String label) {
        if (label == null || label.isBlank()) {
            return "Unknown Species";
        }
        return label.trim();
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace('_', ' ');
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private record VisionLabel(String description, double score) {
    }
}