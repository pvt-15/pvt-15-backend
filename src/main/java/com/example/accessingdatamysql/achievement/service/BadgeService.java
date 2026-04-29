package com.example.accessingdatamysql.achievement.service;

import com.example.accessingdatamysql.achievement.entity.BadgeDefinition;
import com.example.accessingdatamysql.achievement.entity.UserBadge;
import com.example.accessingdatamysql.achievement.repository.BadgeDefinitionRepository;
import com.example.accessingdatamysql.achievement.repository.UserBadgeRepository;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BadgeService {

    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserDiscoveryRepository userDiscoveryRepository;

    public BadgeService(BadgeDefinitionRepository badgeDefinitionRepository,
                        UserBadgeRepository userBadgeRepository,
                        UserDiscoveryRepository userDiscoveryRepository) {
        this.badgeDefinitionRepository = badgeDefinitionRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userDiscoveryRepository = userDiscoveryRepository;
    }

    public void checkAndUnlockCategoryBadges(User user, PictureCategory category) {
        long uniqueCount = userDiscoveryRepository.countByUserAndCategory(user, category);

        List<BadgeDefinition> badgeDefinitions = badgeDefinitionRepository.findByActiveTrueAndCategory(category);

        for (BadgeDefinition badgeDefinition : badgeDefinitions) {
            if (uniqueCount >= badgeDefinition.getRequiredCount()) {
                boolean alreadyUnlocked = userBadgeRepository.existsByUserAndBadgeDefinition(user, badgeDefinition);

                if(!alreadyUnlocked){
                    UserBadge userBadge = new UserBadge();
                    userBadge.setUser(user);
                    userBadge.setBadgeDefinition(badgeDefinition);
                    userBadge.setUnlockedAt(LocalDateTime.now());
                    userBadgeRepository.save(userBadge);
                }
            }
        }
    }
}
