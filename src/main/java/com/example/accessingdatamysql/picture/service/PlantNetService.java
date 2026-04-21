package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Service
public class PlantNetService {
    private static final String IMAGE_URL_REQUIRED = "Image URL is required";
    private static final String PLANTNET_API_KEY_MISSING = "PlantNet API key is missing";
    private static final String IMAGE_DOWNLOAD_FAILED = "Could not download image for PlantNet";
    private static final String PLANTNET_REQUEST_FAILED = "PlantNet request failed";
    private static final String PLANTNET_ENDPOINT = "https://my-api.plantnet.org/v2/identify/";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String plantNetApiKey;
    private final String project;

    public PlantNetService(@Value("${plantnet.api.key:}") String plantNetApiKey,
                           @Value("${plantnet.project:all}") String project,
                           ObjectMapper objectMapper){
        this.httpClient = HttpClient.newHttpClient();
        this.plantNetApiKey = plantNetApiKey;
        this.objectMapper = objectMapper;
        this.project = project == null || project.isBlank() ? "all" : project;
    }

    public AiIdentificationResult identifyPlant(String imageUrl){
        if(imageUrl == null || imageUrl.isBlank()){
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        if(plantNetApiKey == null || plantNetApiKey.isBlank()){
            throw new IllegalStateException(PLANTNET_API_KEY_MISSING);
        }

        try{
            DownloadedImage downloadedImage = downloadImage(imageUrl);
            HttpRequest request = buildPlantNetRequest(downloadedImage);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() < 200 || response.statusCode() >= 300){
                throw new IllegalStateException(
                        PLANTNET_REQUEST_FAILED + " with status " + response.statusCode() + ": " + response.body()
                );
            }
            return parsePlantNetResponse(response.body());
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException(PLANTNET_REQUEST_FAILED, e);
        }catch(IOException e){
            throw new IllegalStateException(PLANTNET_REQUEST_FAILED, e);
        }
    }
    private DownloadedImage downloadImage(String imageUrl) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if(response.statusCode() < 200 || response.statusCode() >= 300){
            throw new IllegalStateException(IMAGE_DOWNLOAD_FAILED + " with status " + response.statusCode());
        }

        String contentType = response.headers()
                .firstValue("Content-Type")
                .map(this::normalizeContentType)
                .orElseGet(() -> inferContentTypeFromUrl(imageUrl));

        if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")){
            contentType = inferContentTypeFromUrl(imageUrl);
        }
        String filename = extractFilename(imageUrl, contentType);
        return new DownloadedImage(response.body(), contentType, filename);
    }

    private HttpRequest buildPlantNetRequest(DownloadedImage downloadedImage) throws IOException{
        String boundary = "Boundary-" + UUID.randomUUID();
        byte[] body = buildMultipartBody(boundary, downloadedImage);

        return HttpRequest.newBuilder()
                .uri(URI.create(PLANTNET_ENDPOINT + project + "?api-key=" + plantNetApiKey))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
    }

    private byte[] buildMultipartBody(String boundary, DownloadedImage downloadedImage) throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        writeFormField(output, boundary, "organs", "auto");
        writeFormField(output, boundary, "no-reject", "true");
        writeFormField(output, boundary, "nb-results", "3");
        writeFormField(output, boundary, "lang", "en");
        writeFileField(output, boundary, "images", downloadedImage.fileName(), downloadedImage.contentType(), downloadedImage.bytes());

        output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private void writeFormField(ByteArrayOutputStream output, String boundary, String name, String value) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(value.getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFileField(ByteArrayOutputStream output,
                                String boundary,
                                String fieldName,
                                String filename,
                                String contentType,
                                byte[] bytes) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(bytes);
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private AiIdentificationResult parsePlantNetResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode results = root.path("results");

        if (!results.isArray() || results.isEmpty()) {
            return new AiIdentificationResult("Unknown Plant", "UNKNOWN", 0.0);
        }

        JsonNode bestResult = results.path(0);
        double confidence = bestResult.path("score").asDouble(0.0);

        JsonNode speciesNode = bestResult.path("species");
        String scientificName = firstNonBlank(
                speciesNode.path("scientificNameWithoutAuthor").asText(null),
                speciesNode.path("scientificName").asText(null),
                root.path("bestMatch").asText(null),
                "Unknown Plant"
        );

        String commonName = speciesNode.path("commonNames").isArray() && !speciesNode.path("commonNames").isEmpty()
                ? speciesNode.path("commonNames").path(0).asText("").trim()
                : "";

        String predictedOrgan = root.path("predictedOrgans").isArray() && !root.path("predictedOrgans").isEmpty()
                ? root.path("predictedOrgans").path(0).path("organ").asText("")
                : "";

        String familyName = speciesNode.path("family").path("scientificNameWithoutAuthor").asText("");
        String label = commonName.isBlank() ? scientificName : commonName;
        String category = mapPlantCategory(label, scientificName, familyName, predictedOrgan);

        return new AiIdentificationResult(label, category, confidence);
    }

    private String mapPlantCategory(String label, String scientificName, String familyName, String predictedOrgan) {
        String combined = normalize(label) + " " + normalize(scientificName) + " " + normalize(familyName);

        if (containsAny(normalize(predictedOrgan), "flower")) {
            return "FLOWER";
        }
        if (containsAny(combined,
                "pine", "pinus", "spruce", "picea", "fir", "abies", "birch", "betula", "oak", "quercus",
                "maple", "acer", "ash", "fraxinus", "alder", "alnus", "willow", "salix", "rowan", "sorbus",
                "juniper", "juniperus", "hazel", "corylus", "linden", "tilia", "elm", "ulmus", "beech", "fagus",
                "pinaceae", "fagaceae", "betulaceae", "salicaceae")) {
            return "TREE";
        }
        if (containsAny(combined,
                "flower", "rose", "rosa", "tulip", "tulipa", "daisy", "bellis", "dandelion", "taraxacum",
                "lily", "lilium", "orchid", "orchidaceae", "violet", "viola", "anemone", "anemone", "crocus",
                "buttercup", "ranunculus", "poppy", "papaver", "bluebell", "hyacinthoides")) {
            return "FLOWER";
        }
        return "PLANT";
    }

    private String normalizeContentType(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT).trim();
        int semicolonIndex = normalized.indexOf(';');
        if (semicolonIndex >= 0) {
            normalized = normalized.substring(0, semicolonIndex).trim();
        }
        if (normalized.equals("image/jpg")) {
            return "image/jpeg";
        }
        return normalized;
    }

    private String inferContentTypeFromUrl(String imageUrl) {
        String lower = imageUrl.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "image/jpeg";
    }

    private String extractFilename(String imageUrl, String contentType) {
        String cleanUrl = imageUrl;
        int questionMark = cleanUrl.indexOf('?');
        if (questionMark >= 0) {
            cleanUrl = cleanUrl.substring(0, questionMark);
        }

        int slashIndex = cleanUrl.lastIndexOf('/');
        String filename = slashIndex >= 0 ? cleanUrl.substring(slashIndex + 1) : cleanUrl;
        if (filename.isBlank()) {
            filename = contentType.equals("image/png") ? "plant.png" : "plant.jpg";
        }
        return filename;
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "Unknown Plant";
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace('_', ' ')
                .trim();
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private record DownloadedImage(byte[] bytes, String contentType, String fileName){
    }
}
