package com.example.accessingdatamysql.achievement.service;

import com.example.accessingdatamysql.achievement.entity.BadgeDefinition;
import com.example.accessingdatamysql.achievement.entity.UserBadge;
import com.example.accessingdatamysql.achievement.enums.BadgeTier;
import com.example.accessingdatamysql.achievement.repository.BadgeDefinitionRepository;
import com.example.accessingdatamysql.achievement.repository.UserBadgeRepository;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.repository.UserDiscoveryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeDefinitionRepository badgeDefinitionRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private UserDiscoveryRepository userDiscoveryRepository;

    @InjectMocks
    private BadgeService badgeService;

    @Test
    void checkAndUnlockCategoryBadges_shouldSaveBadgeWhenThresholdIsReached() {
        User user = new User();
        user.setId(1);

        BadgeDefinition badgeDefinition = new BadgeDefinition();
        badgeDefinition.setId(10);
        badgeDefinition.setCode("FLOWER_BRONZE");
        badgeDefinition.setName("Flower Bronze Badge");
        badgeDefinition.setDescription("Find 10 unique flowers");
        badgeDefinition.setCategory(PictureCategory.FLOWER);
        badgeDefinition.setTier(BadgeTier.BRONZE);
        badgeDefinition.setRequiredCount(10);
        badgeDefinition.setActive(true);

        when(userDiscoveryRepository.countByUserAndCategory(user, PictureCategory.FLOWER)).thenReturn(10L);
        when(badgeDefinitionRepository.findByActiveTrueAndCategory(PictureCategory.FLOWER))
                .thenReturn(List.of(badgeDefinition));
        when(userBadgeRepository.existsByUserAndBadgeDefinition(user, badgeDefinition)).thenReturn(false);

        badgeService.checkAndUnlockCategoryBadges(user, PictureCategory.FLOWER);

        ArgumentCaptor<UserBadge> captor = ArgumentCaptor.forClass(UserBadge.class);
        verify(userBadgeRepository).save(captor.capture());

        UserBadge saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(badgeDefinition, saved.getBadgeDefinition());
    }

    @Test
    void checkAndUnlockCategoryBadges_shouldNotSaveWhenBadgeAlreadyUnlocked() {
        User user = new User();
        user.setId(1);

        BadgeDefinition badgeDefinition = new BadgeDefinition();
        badgeDefinition.setId(10);
        badgeDefinition.setCategory(PictureCategory.FLOWER);
        badgeDefinition.setTier(BadgeTier.BRONZE);
        badgeDefinition.setRequiredCount(10);
        badgeDefinition.setActive(true);

        when(userDiscoveryRepository.countByUserAndCategory(user, PictureCategory.FLOWER)).thenReturn(15L);
        when(badgeDefinitionRepository.findByActiveTrueAndCategory(PictureCategory.FLOWER))
                .thenReturn(List.of(badgeDefinition));
        when(userBadgeRepository.existsByUserAndBadgeDefinition(user, badgeDefinition)).thenReturn(true);

        badgeService.checkAndUnlockCategoryBadges(user, PictureCategory.FLOWER);

        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }
}