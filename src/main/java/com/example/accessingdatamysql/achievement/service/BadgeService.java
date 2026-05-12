package com.example.accessingdatamysql.achievement.service;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    private String getBadgeImageUrl(BadgeDefinition badge) {
        if (badge == null || badge.getCode() == null || badge.getCode().isBlank()) {
            return null;
        }

        String objectKey = BADGE_ICON_FOLDER + badge.getCode() + ".png";
        return imageStorageService.generateSignedReadUrl(objectKey);
    }
}
