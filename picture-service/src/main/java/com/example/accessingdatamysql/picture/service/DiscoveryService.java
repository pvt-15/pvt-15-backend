package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.gamification.ScoringRules;
import com.example.accessingdatamysql.picture.dto.DiscoveryCategoryStatsResponse;
import com.example.accessingdatamysql.picture.dto.DiscoveryStatsResponse;
import com.example.accessingdatamysql.picture.dto.LibraryItemResponse;
import com.example.accessingdatamysql.picture.entity.UserDiscovery;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiscoveryService {

    private final UserDiscoveryRepository userDiscoveryRepository;
    private final ImageStorageService imageStorageService;

    public DiscoveryService(UserDiscoveryRepository userDiscoveryRepository,
                            ImageStorageService imageStorageService) {
        this.userDiscoveryRepository = userDiscoveryRepository;
        this.imageStorageService = imageStorageService;
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

    private String getSignedUrlOrFallback(UserDiscovery discovery) {
        String imageObjectKey = discovery.getImageObjectKey();

        if (imageObjectKey != null && !imageObjectKey.isBlank()) {
            return imageStorageService.generateSignedReadUrl(imageObjectKey);
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