package com.example.accessingdatamysql.achievement.service;

import com.example.accessingdatamysql.achievement.dto.BadgeProgressResponse;
import com.example.accessingdatamysql.achievement.dto.BadgeResponse;
import com.example.accessingdatamysql.achievement.entity.BadgeDefinition;
import com.example.accessingdatamysql.achievement.entity.UserBadge;
import com.example.accessingdatamysql.achievement.repository.BadgeDefinitionRepository;
import com.example.accessingdatamysql.achievement.repository.UserBadgeRepository;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BadgeService {

    private static final String BADGE_ICON_FOLDER = "badge-icons/";

    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserDiscoveryRepository userDiscoveryRepository;
    private final ImageStorageService imageStorageService;

    public BadgeService(BadgeDefinitionRepository badgeDefinitionRepository,
                        UserBadgeRepository userBadgeRepository,
                        UserDiscoveryRepository userDiscoveryRepository,
                        ImageStorageService imageStorageService) {
        this.badgeDefinitionRepository = badgeDefinitionRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userDiscoveryRepository = userDiscoveryRepository;
        this.imageStorageService = imageStorageService;
    }

    public void checkAndUnlockCategoryBadges(User user, PictureCategory category) {
        long uniqueCount = userDiscoveryRepository.countByUserAndCategory(user, category);

        List<BadgeDefinition> badgeDefinitions = badgeDefinitionRepository.findByActiveTrueAndCategory(category);

        for (BadgeDefinition badgeDefinition : badgeDefinitions) {
            if (uniqueCount >= badgeDefinition.getRequiredCount()) {
                boolean alreadyUnlocked = userBadgeRepository.existsByUserAndBadgeDefinition(user, badgeDefinition);

                if (!alreadyUnlocked) {
                    UserBadge userBadge = new UserBadge();
                    userBadge.setUser(user);
                    userBadge.setBadgeDefinition(badgeDefinition);
                    userBadge.setUnlockedAt(LocalDateTime.now());
                    userBadgeRepository.save(userBadge);
                }
            }
        }
    }

    public List<BadgeResponse> getMyBadges(User user) {
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
        List<BadgeResponse> responses = new ArrayList<>();

        userBadges.sort(Comparator.comparing(UserBadge::getUnlockedAt).reversed());

        for(UserBadge userBadge : userBadges){
            BadgeDefinition badge = userBadge.getBadgeDefinition();

            String imageUrl = getBadgeImageUrl(badge);

            responses.add(new BadgeResponse(
                    userBadge.getId(),
                    badge.getCode(),
                    badge.getName(),
                    badge.getDescription(),
                    badge.getCategory().name(),
                    badge.getTier().name(),
                    badge.getRequiredCount(),
                    userBadge.getUnlockedAt() != null ? userBadge.getUnlockedAt().toString() : null,
                    imageUrl
            ));
        }
        return responses;
    }

    public List<BadgeProgressResponse> getAllBadgesForUser(User user) {
        List<BadgeDefinition> badgeDefinitions = badgeDefinitionRepository.findByActiveTrue();
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);

        Map<Integer, UserBadge> unlockedByBadgeDefinitionId = new HashMap<>();

        for (UserBadge userBadge : userBadges) {
            BadgeDefinition badgeDefinition = userBadge.getBadgeDefinition();

            if (badgeDefinition != null && badgeDefinition.getId() != null) {
                unlockedByBadgeDefinitionId.put(badgeDefinition.getId(), userBadge);
            }
        }

        badgeDefinitions.sort(
                Comparator.comparing((BadgeDefinition badge) -> badge.getCategory().name())
                        .thenComparing(badge -> badge.getTier().ordinal())
        );

        List<BadgeProgressResponse> responses = new ArrayList<>();

        for (BadgeDefinition badgeDefinition : badgeDefinitions) {
            UserBadge unlockedBadge = unlockedByBadgeDefinitionId.get(badgeDefinition.getId());
            boolean unlocked = unlockedBadge != null;

            Long currentCount = userDiscoveryRepository.countByUserAndCategory(
                    user,
                    badgeDefinition.getCategory()
            );

            String unlockedAt = null;
            Integer userBadgeId = null;

            if (unlockedBadge != null) {
                userBadgeId = unlockedBadge.getId();

                if (unlockedBadge.getUnlockedAt() != null) {
                    unlockedAt = unlockedBadge.getUnlockedAt().toString();
                }
            }

            responses.add(new BadgeProgressResponse(
                    badgeDefinition.getId(),
                    userBadgeId,
                    badgeDefinition.getCode(),
                    badgeDefinition.getName(),
                    badgeDefinition.getDescription(),
                    badgeDefinition.getCategory().name(),
                    badgeDefinition.getTier().name(),
                    badgeDefinition.getRequiredCount(),
                    currentCount,
                    unlocked,
                    unlockedAt,
                    getBadgeImageUrl(badgeDefinition)
            ));
        }

        return responses;
    }

    private String getBadgeImageUrl(BadgeDefinition badge) {
        if (badge == null || badge.getCode() == null || badge.getCode().isBlank()) {
            return null;
        }

        String objectKey = BADGE_ICON_FOLDER + badge.getCode() + ".png";
        return imageStorageService.generateSignedReadUrl(objectKey);
    }
}
