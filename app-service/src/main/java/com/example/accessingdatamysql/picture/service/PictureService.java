package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.achievement.dto.BadgeResponse;
import com.example.accessingdatamysql.achievement.service.BadgeService;
import com.example.accessingdatamysql.gamification.UserProgressionService;
import com.example.accessingdatamysql.gamification.dto.GamificationUpdateResponse;
import com.example.accessingdatamysql.model.challenge.dto.DailyChallengePictureRequest;
import com.example.accessingdatamysql.model.challenge.service.ChallengeProgressService;
import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureCreateResultResponse;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.picture.dto.PictureStatsResponse;
import com.example.accessingdatamysql.picture.entity.Picture;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.enums.PictureRejectionReason;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.storage.client.StorageClient;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PictureService {

    private static final double MIN_VISION_CONFIDENCE = 0.80;
    private static final double MIN_PLANTNET_CONFIDENCE = 0.15;

    private static final String USER_NOT_FOUND = "User not found";
    private static final String PICTURE_NOT_FOUND = "Picture not found";
    private static final String REQUEST_BODY_REQUIRED = "Request body is required";
    private static final String IMAGE_OBJECT_KEY_REQUIRED = "Image object key is required";
    private static final String CHALLENGE_ID_REQUIRED = "Challenge id is required when pictureMode is CHALLENGE";
    private static final String LOW_CONFIDENCE_ERROR = "The image quality was too low. Please retake the picture.";
    private static final String COLLECTION_NOT_RELEVANT_ERROR = "The picture could not be identified as a relevant animal or plant.";
    private static final String CHALLENGE_NO_MATCH_ERROR = "The picture did not match any active challenge task.";


    private final PictureRepository pictureRepository;
    private final UserRepository userRepository;
    private final NatureAiService natureAiService;
    private final DiscoveryService discoveryService;
    private final ChallengeProgressService challengeProgressService;
    private final BadgeService badgeService;
    private final UserProgressionService userProgressionService;
    private final StorageClient storageClient;

    public PictureService(PictureRepository pictureRepository,
                          UserRepository userRepository,
                          NatureAiService natureAiService,
                          DiscoveryService discoveryService,
                          ChallengeProgressService challengeProgressService,
                          BadgeService badgeService,
                          UserProgressionService userProgressionService,
                          StorageClient storageClient) {
        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
        this.natureAiService = natureAiService;
        this.discoveryService = discoveryService;
        this.challengeProgressService = challengeProgressService;
        this.badgeService = badgeService;
        this.userProgressionService = userProgressionService;
        this.storageClient = storageClient;
    }

    @Transactional
    public PictureCreateResultResponse createPicture(Integer userId, CreatePictureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }

        String imageObjectKey = request.getImageObjectKey();
        if (imageObjectKey == null || imageObjectKey.isBlank()) {
            throw new IllegalArgumentException(IMAGE_OBJECT_KEY_REQUIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        Level previousLevel = user.getLevel();

        PictureMode pictureMode = request.getPictureMode();
        if (pictureMode == null) {
            pictureMode = PictureMode.COLLECTION;
        }

        Integer challengeId = request.getChallengeId();
        if (pictureMode == PictureMode.CHALLENGE && challengeId == null) {
            throw new IllegalArgumentException(CHALLENGE_ID_REQUIRED);
        }

        TargetType targetType = request.getTargetType();
        if (targetType == null) {
            targetType = TargetType.ANIMAL;
        }

        String signedImageUrl = storageClient.generateSignedReadUrl(imageObjectKey);

        AiIdentificationResult aiResult = natureAiService.identifyImage(signedImageUrl, targetType);
        PictureCategory pictureCategory = parseCategory(aiResult.getCategory());

        if (isLowConfidence(aiResult)) {
            deleteImageQuietly(imageObjectKey);
            return PictureCreateResultResponse.rejected(PictureRejectionReason.LOW_CONFIDENCE, LOW_CONFIDENCE_ERROR);
        }

        if (pictureMode == PictureMode.COLLECTION && !isAcceptedForCollection(pictureCategory, aiResult)) {
            deleteImageQuietly(imageObjectKey);
            return PictureCreateResultResponse.rejected(PictureRejectionReason.COLLECTION_NOT_RELEVANT, COLLECTION_NOT_RELEVANT_ERROR);
        }

        if (pictureMode == PictureMode.CHALLENGE) {
            boolean matchesChallenge = challengeProgressService.matchesAnyTask(
                    user,
                    challengeId,
                    pictureCategory,
                    aiResult.getLabel()
            );

            if (!matchesChallenge) {
                deleteImageQuietly(imageObjectKey);
                return PictureCreateResultResponse.rejected(PictureRejectionReason.CHALLENGE_NO_MATCH, CHALLENGE_NO_MATCH_ERROR);
            }
        }

        Picture picture = new Picture();
        picture.setLabel(aiResult.getLabel());
        picture.setCategory(pictureCategory);
        picture.setAiConfidence(aiResult.getAiConfidence());
        picture.setImageObjectKey(imageObjectKey);
        picture.setTakenAt(LocalDateTime.now());
        picture.setPictureMode(pictureMode);
        picture.setUser(user);

        int discoveryPoints = 0;

        boolean shouldRecordDiscovery =
                pictureMode == PictureMode.COLLECTION || pictureMode == PictureMode.CHALLENGE;

        if (shouldRecordDiscovery && isAcceptedForCollection(pictureCategory, aiResult)) {
            discoveryPoints = discoveryService.awardDiscoveryPoints(
                    user,
                    pictureCategory,
                    aiResult.getLabel(),
                    imageObjectKey
            );
        }

        picture.setPointsAwarded(discoveryPoints);

        Picture savedPicture = pictureRepository.save(picture);

        if (discoveryPoints > 0) {
            userProgressionService.applyAward(user, discoveryPoints);
        }

        if (pictureMode == PictureMode.CHALLENGE) {
            int challengeReward = challengeProgressService.updateProgressFromPicture(
                    user,
                    savedPicture,
                    challengeId
            );

            if (challengeReward > 0) {
                userProgressionService.applyAward(user, challengeReward);
            }
        }

        List<BadgeResponse> newlyUnlockedBadges = List.of();

        if (shouldRecordDiscovery && pictureCategory != PictureCategory.UNKNOWN) {
            newlyUnlockedBadges = badgeService.checkAndUnlockCategoryBadges(user, pictureCategory);
        }

        GamificationUpdateResponse gamification = new GamificationUpdateResponse(
                user.getLevel() != previousLevel,
                previousLevel.name(),
                user.getLevel().name(),
                newlyUnlockedBadges
        );

        return PictureCreateResultResponse.accepted(
                toResponse(savedPicture),
                gamification
        );
    }

    @Transactional
    public PictureCreateResultResponse createDailyChallengePicture(
            Integer userId,
            Integer challengeId,
            DailyChallengePictureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }

        String imageObjectKey = request.getImageObjectKey();

        if (imageObjectKey == null || imageObjectKey.isBlank()) {
            throw new IllegalArgumentException(IMAGE_OBJECT_KEY_REQUIRED);
        }

        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge id is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        Level previousLevel = user.getLevel();

        Picture picture = new Picture();
        picture.setLabel("Daily challenge submission");
        picture.setCategory(PictureCategory.UNKNOWN);
        picture.setAiConfidence(0.0);
        picture.setPointsAwarded(0);
        picture.setImageObjectKey(imageObjectKey);
        picture.setTakenAt(LocalDateTime.now());
        picture.setPictureMode(PictureMode.DAILY_CHALLENGE);
        picture.setUser(user);

        Picture savedPicture = pictureRepository.save(picture);

        int challengeReward = challengeProgressService.updateProgressFromDailyPicture(
                user,
                savedPicture,
                challengeId
        );

        if (challengeReward > 0) {
            userProgressionService.applyAward(user, challengeReward);
        }

        GamificationUpdateResponse gamification = new GamificationUpdateResponse(
                user.getLevel() != previousLevel,
                previousLevel.name(),
                user.getLevel().name(),
                List.of()
        );

        return PictureCreateResultResponse.accepted(
                toResponse(savedPicture),
                gamification
        );
    }

    public List<PictureResponse> getMyPictures(Integer userId, String category, String sort) {
        User user = getUserById(userId);

        List<Picture> pictures = new ArrayList<>(pictureRepository.findByUser(user));

        if (category != null && !category.isBlank()) {
            pictures = filterByCategory(pictures, category);
        }

        sortPictures(pictures, sort);

        List<PictureResponse> responses = new ArrayList<>();
        for (Picture picture : pictures) {
            responses.add(toResponse(picture));
        }

        return responses;
    }

    public PictureResponse getPictureById(Integer userId, Integer pictureId) {
        User user = getUserById(userId);
        Picture picture = getPictureOwnedByUser(user, pictureId);

        return toResponse(picture);
    }

    @Transactional
    public void deletePicture(Integer userId, Integer pictureId) {
        User user = getUserById(userId);
        Picture picture = getPictureOwnedByUser(user, pictureId);

        String imageObjectKey = picture.getImageObjectKey();

        discoveryService.handlePictureDeleted(user, picture);
        challengeProgressService.handlePictureDeleted(picture);

        pictureRepository.delete(picture);
        deleteImageQuietly(imageObjectKey);
    }

    public PictureStatsResponse getPictureStats(Integer userId) {
        User user = getUserById(userId);
        List<Picture> pictures = pictureRepository.findByUser(user);

        int totalPictures = pictures.size();
        int totalPlants = 0;
        int totalAnimals = 0;
        int totalFlowers = 0;
        int totalTrees = 0;
        int totalBirds = 0;
        int totalInsects = 0;
        int totalPoints = user.getTotalPoints();

        for (Picture picture : pictures) {
            PictureCategory category = picture.getCategory();

            if (category == PictureCategory.PLANT) {
                totalPlants++;
            } else if (category == PictureCategory.ANIMAL) {
                totalAnimals++;
            } else if (category == PictureCategory.FLOWER) {
                totalFlowers++;
                totalPlants++;
            } else if (category == PictureCategory.TREE) {
                totalTrees++;
                totalPlants++;
            } else if (category == PictureCategory.BIRD) {
                totalBirds++;
                totalAnimals++;
            } else if (category == PictureCategory.INSECT) {
                totalInsects++;
                totalAnimals++;
            }
        }

        return new PictureStatsResponse(
                totalPictures,
                totalPlants,
                totalAnimals,
                totalFlowers,
                totalTrees,
                totalBirds,
                totalInsects,
                totalPoints
        );
    }

    private boolean isLowConfidence(AiIdentificationResult aiResult) {
        if (aiResult == null) {
            return true;
        }

        if (aiResult.getAiProvider() == null) {
            return true;
        }

        return switch (aiResult.getAiProvider()) {
            case GOOGLE_VISION -> aiResult.getAiConfidence() < MIN_VISION_CONFIDENCE;
            case PLANTNET -> aiResult.getAiConfidence() < MIN_PLANTNET_CONFIDENCE;
        };
    }

    private boolean isAcceptedForCollection(PictureCategory category, AiIdentificationResult aiResult) {
        if (aiResult == null) {
            return false;
        }

        if (category == null || category == PictureCategory.UNKNOWN) {
            return false;
        }

        String label = aiResult.getLabel();
        return label != null && !label.isBlank();
    }

    private void deleteImageQuietly(String imageObjectKey) {
        if (imageObjectKey == null || imageObjectKey.isBlank()) {
            return;
        }

        try {
            storageClient.deleteImage(imageObjectKey);
        } catch (Exception e) {
            System.err.println("Failed to delete image from storage: " + imageObjectKey);
        }
    }

    private PictureCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return PictureCategory.UNKNOWN;
        }

        try {
            return PictureCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PictureCategory.UNKNOWN;
        }
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    private Picture getPictureOwnedByUser(User user, Integer pictureId) {
        Picture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new IllegalArgumentException(PICTURE_NOT_FOUND));

        if (!picture.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(PICTURE_NOT_FOUND);
        }

        return picture;
    }

    private List<Picture> filterByCategory(List<Picture> pictures, String category) {
        List<Picture> filteredPictures = new ArrayList<>();

        for (Picture picture : pictures) {
            if (picture.getCategory() != null
                    && picture.getCategory().name().equalsIgnoreCase(category)) {
                filteredPictures.add(picture);
            }
        }

        return filteredPictures;
    }

    private void sortPictures(List<Picture> pictures, String sort) {
        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("newest")) {
            pictures.sort(Comparator.comparing(Picture::getTakenAt).reversed());
            return;
        }

        if (sort.equalsIgnoreCase("oldest")) {
            pictures.sort(Comparator.comparing(Picture::getTakenAt));
        }
    }

    private PictureResponse toResponse(Picture picture) {
        String imageUrl = getSignedUrlOrFallback(picture);

        String category = null;
        if (picture.getCategory() != null) {
            category = picture.getCategory().name();
        }

        return new PictureResponse(
                picture.getId(),
                picture.getLabel(),
                category,
                picture.getAiConfidence(),
                picture.getPointsAwarded(),
                imageUrl,
                picture.getTakenAt().toString(),
                picture.getPictureMode()
        );
    }

    private String getSignedUrlOrFallback(Picture picture) {
        String imageObjectKey = picture.getImageObjectKey();

        if (imageObjectKey != null && !imageObjectKey.isBlank()) {
            return storageClient.generateSignedReadUrl(imageObjectKey);
        }

        return picture.getImageUrl();
    }
}