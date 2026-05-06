package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.achievement.service.BadgeService;
import com.example.accessingdatamysql.gamification.UserProgressionService;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.model.challenge.service.ChallengeProgressService;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.picture.dto.PictureStatsResponse;
import com.example.accessingdatamysql.picture.entity.Picture;
import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PictureService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String PICTURE_NOT_FOUND = "Picture not found";
    private static final String REQUEST_BODY_REQUIRED = "Request body is required";
    private static final String IMAGE_URL_REQUIRED = "Image URL is required";

    private final PictureRepository pictureRepository;
    private final UserRepository userRepository;
    private final NatureAiService natureAiService;
    private final DiscoveryService discoveryService;
    private final ChallengeProgressService challengeProgressService;
    private final BadgeService badgeService;
    private final UserProgressionService userProgressionService;
    private final ImageStorageService imageStorageService;

    public PictureService(PictureRepository pictureRepository,
                          UserRepository userRepository,
                          NatureAiService natureAiService,
                          DiscoveryService discoveryService,
                          ChallengeProgressService challengeProgressService,
                          BadgeService badgeService,
                          UserProgressionService userProgressionService,
                          ImageStorageService imageStorageService) {
        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
        this.natureAiService = natureAiService;
        this.discoveryService = discoveryService;
        this.challengeProgressService = challengeProgressService;
        this.badgeService = badgeService;
        this.userProgressionService = userProgressionService;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public PictureResponse createPicture(Integer userId, CreatePictureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(REQUEST_BODY_REQUIRED);
        }
        String imageUrl = request.getImageUrl();

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException(IMAGE_URL_REQUIRED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        PictureMode pictureMode = request.getPictureMode();
        if(pictureMode == null){
            pictureMode = PictureMode.COLLECTION;
        }

        TargetType targetType = request.getTargetType();
        if (targetType == null) {
            targetType = TargetType.ANIMAL;
        }
        AiIdentificationResult aiResult = natureAiService.identifyImage(imageUrl, targetType);

        PictureCategory pictureCategory = parseCategory(aiResult.getCategory(), targetType);

        Picture picture = new Picture();
        picture.setLabel(aiResult.getLabel());
        picture.setCategory(parseCategory(aiResult.getCategory(), targetType));
        picture.setAiConfidence(aiResult.getAiConfidence());
        picture.setImageUrl(imageUrl);
        picture.setTakenAt(LocalDateTime.now());
        picture.setPictureMode(pictureMode);
        picture.setUser(user);

        int picturePoints = discoveryService.awardDiscoveryPoints(
                user,
                pictureCategory,
                aiResult.getLabel(),
                imageUrl
        );

        picture.setPointsAwarded(picturePoints);

        Picture savedPicture = pictureRepository.save(picture);

        int totalPointsToAward = picturePoints;

        if (picturePoints > 0) {
            badgeService.checkAndUnlockCategoryBadges(user, pictureCategory);
        }

        if (pictureMode == PictureMode.CHALLENGE) {
            int challengeReward = challengeProgressService.updateProgressFromPicture(user, savedPicture);
            totalPointsToAward += challengeReward;
        }

        userProgressionService.applyAward(user, totalPointsToAward);

        return toResponse(savedPicture);
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
        pictureRepository.delete(picture);
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
            if (picture.getCategory().name().equalsIgnoreCase(category)) {
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

    private PictureCategory parseCategory(String category, TargetType targetType) {
        if (category == null || category.isBlank()) {
            return defaultCategory(targetType);
        }
        try {
            return PictureCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultCategory(targetType);
        }
    }

    private PictureCategory defaultCategory(TargetType targetType) {
        if (targetType == TargetType.PLANT) {
            return PictureCategory.PLANT;
        }
        return PictureCategory.ANIMAL;
    }

    private PictureResponse toResponse(Picture picture) {
        return new PictureResponse(
                picture.getId(),
                picture.getLabel(),
                picture.getCategory().name(),
                picture.getAiConfidence(),
                picture.getPointsAwarded(),
                picture.getImageUrl(),
                picture.getTakenAt().toString(),
                picture.getPictureMode()
        );
    }
}