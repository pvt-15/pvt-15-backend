package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.gamification.ScoringRules;
import com.example.accessingdatamysql.picture.dto.DiscoveryCategoryStatsResponse;
import com.example.accessingdatamysql.picture.dto.DiscoveryStatsResponse;
import com.example.accessingdatamysql.picture.dto.LibraryItemResponse;
import com.example.accessingdatamysql.picture.entity.Picture;
import com.example.accessingdatamysql.picture.entity.UserDiscovery;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import com.example.accessingdatamysql.storage.client.StorageClient;
import com.example.accessingdatamysql.user.entity.User;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DiscoveryService {

    private final UserDiscoveryRepository userDiscoveryRepository;
    private final PictureRepository pictureRepository;
    private final StorageClient storageClient;

    public DiscoveryService(
            UserDiscoveryRepository userDiscoveryRepository,
            PictureRepository pictureRepository,
            StorageClient storageClient
    ) {
        this.userDiscoveryRepository = userDiscoveryRepository;
        this.pictureRepository = pictureRepository;
        this.storageClient = storageClient;
    }

    public int awardDiscoveryPoints(User user,
                                    PictureCategory pictureCategory,
                                    String label,
                                    String imageObjectKey) {
        String normalizedLabel = normalize(label);

        boolean alreadyExists = userDiscoveryRepository.existsByUserAndCategoryAndNormalizedLabel(
                user,
                pictureCategory,
                normalizedLabel
        );

        if (alreadyExists) {
            return 0;
        }

        UserDiscovery discovery = new UserDiscovery();
        discovery.setUser(user);
        discovery.setCategory(pictureCategory);
        discovery.setNormalizedLabel(normalizedLabel);
        discovery.setDiscoveredAt(LocalDateTime.now());
        discovery.setDisplayLabel(label);
        discovery.setImageObjectKey(imageObjectKey);

        userDiscoveryRepository.save(discovery);

        long uniqueCountInCategory = userDiscoveryRepository.countByUserAndCategory(user, pictureCategory);

        int points = ScoringRules.NEW_UNIQUE_DISCOVERY_POINTS;

        if (uniqueCountInCategory % ScoringRules.DISCOVERY_MILESTONE_SIZE == 0) {
            points += ScoringRules.DISCOVERY_MILESTONE_BONUS;
        }

        return points;
    }

    public DiscoveryStatsResponse getDiscoveryStats(User user) {
        List<DiscoveryCategoryStatsResponse> categories = new ArrayList<>();

        categories.add(createCategoryStats(user, PictureCategory.FLOWER));
        categories.add(createCategoryStats(user, PictureCategory.TREE));
        categories.add(createCategoryStats(user, PictureCategory.PLANT));
        categories.add(createCategoryStats(user, PictureCategory.ANIMAL));
        categories.add(createCategoryStats(user, PictureCategory.BIRD));
        categories.add(createCategoryStats(user, PictureCategory.INSECT));

        return new DiscoveryStatsResponse(categories);
    }

    public List<LibraryItemResponse> getUniqueLibrary(User user, String category, String sort) {
        List<UserDiscovery> discoveries = userDiscoveryRepository.findByUser(user);
        List<LibraryItemResponse> responses = new ArrayList<>();

        for (UserDiscovery discovery : discoveries) {
            if (category != null && !category.isBlank()) {
                if (!discovery.getCategory().name().equalsIgnoreCase(category)) {
                    continue;
                }
            }

            String imageUrl = getSignedUrlOrFallback(discovery);

            responses.add(new LibraryItemResponse(
                    discovery.getId(),
                    discovery.getDisplayLabel(),
                    discovery.getCategory().name(),
                    imageUrl,
                    discovery.getDiscoveredAt().toString()
            ));
        }

        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("newest")) {
            responses.sort((a, b) -> b.getDiscoveredAt().compareTo(a.getDiscoveredAt()));
        } else if (sort.equalsIgnoreCase("oldest")) {
            responses.sort((a, b) -> a.getDiscoveredAt().compareTo(b.getDiscoveredAt()));
        }

        return responses;
    }

    @Transactional
    public void handlePictureDeleted(User user, Picture deletedPicture) {
        if (user == null || deletedPicture == null) {
            return;
        }

        if (deletedPicture.getPictureMode() != PictureMode.COLLECTION) {
            return;
        }

        PictureCategory category = deletedPicture.getCategory();

        if (category == null || category == PictureCategory.UNKNOWN) {
            return;
        }

        String normalizedLabel = normalize(deletedPicture.getLabel());

        if (normalizedLabel.isBlank()) {
            return;
        }

        Optional<UserDiscovery> optionalDiscovery =
                userDiscoveryRepository.findByUserAndCategoryAndNormalizedLabel(
                        user,
                        category,
                        normalizedLabel
                );

        if (optionalDiscovery.isEmpty()) {
            return;
        }

        UserDiscovery discovery = optionalDiscovery.get();

        Picture replacementPicture = findReplacementPicture(user, deletedPicture, category, normalizedLabel);

        if (replacementPicture == null) {
            userDiscoveryRepository.delete(discovery);
            return;
        }

        if (discoveryPointsToDeletedPicture(discovery, deletedPicture)) {
            discovery.setDisplayLabel(replacementPicture.getLabel());
            discovery.setImageObjectKey(replacementPicture.getImageObjectKey());
            discovery.setImageUrl(replacementPicture.getImageUrl());
            userDiscoveryRepository.save(discovery);
        }
    }

    private Picture findReplacementPicture(
            User user,
            Picture deletedPicture,
            PictureCategory category,
            String normalizedLabel
    ) {
        return pictureRepository.findByUser(user).stream()
                .filter(picture -> !picture.getId().equals(deletedPicture.getId()))
                .filter(picture -> picture.getPictureMode() == PictureMode.COLLECTION)
                .filter(picture -> picture.getCategory() == category)
                .filter(picture -> normalize(picture.getLabel()).equals(normalizedLabel))
                .filter(picture -> picture.getImageObjectKey() != null && !picture.getImageObjectKey().isBlank())
                .max(Comparator.comparing(Picture::getTakenAt))
                .orElse(null);
    }

    private boolean discoveryPointsToDeletedPicture(UserDiscovery discovery, Picture deletedPicture) {
        String discoveryObjectKey = discovery.getImageObjectKey();
        String deletedObjectKey = deletedPicture.getImageObjectKey();

        if (discoveryObjectKey != null && deletedObjectKey != null) {
            return discoveryObjectKey.equals(deletedObjectKey);
        }

        String discoveryImageUrl = discovery.getImageUrl();
        String deletedImageUrl = deletedPicture.getImageUrl();

        if (discoveryImageUrl != null && deletedImageUrl != null) {
            return discoveryImageUrl.equals(deletedImageUrl);
        }

        return discoveryObjectKey == null && discoveryImageUrl == null;
    }

    private String getSignedUrlOrFallback(UserDiscovery discovery) {
        String imageObjectKey = discovery.getImageObjectKey();

        if (imageObjectKey != null && !imageObjectKey.isBlank()) {
            return storageClient.generateSignedReadUrl(imageObjectKey);
        }

        return discovery.getImageUrl();
    }

    private DiscoveryCategoryStatsResponse createCategoryStats(User user, PictureCategory category) {
        long uniqueCount = userDiscoveryRepository.countByUserAndCategory(user, category);
        long milestoneSize = ScoringRules.DISCOVERY_MILESTONE_SIZE;
        long nextMilestone = ((uniqueCount / milestoneSize) + 1) * milestoneSize;
        long remainingToNextMilestone = nextMilestone - uniqueCount;

        return new DiscoveryCategoryStatsResponse(
                category.name(),
                uniqueCount,
                nextMilestone,
                remainingToNextMilestone
        );
    }

    private String normalize(String label) {
        if (label == null) {
            return "";
        }

        return label.trim().toLowerCase();
    }
}