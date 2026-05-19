package com.example.accessingdatamysql.achievement.service;

import com.example.accessingdatamysql.achievement.dto.BadgeProgressResponse;
import com.example.accessingdatamysql.achievement.entity.BadgeDefinition;
import com.example.accessingdatamysql.achievement.entity.UserBadge;
import com.example.accessingdatamysql.achievement.enums.BadgeTier;
import com.example.accessingdatamysql.achievement.repository.BadgeDefinitionRepository;
import com.example.accessingdatamysql.achievement.repository.UserBadgeRepository;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.storage.client.StorageClient;
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

    @Mock
    private StorageClient storageClient;

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

        when(userBadgeRepository.save(any(UserBadge.class)))
                .thenAnswer(invocation -> {
                    UserBadge saved = invocation.getArgument(0);
                    saved.setId(123);
                    return saved;
                });

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

    @Test
    void getAllBadgesForUser_shouldReturnAllBadgesWithUnlockedStatusAndImageUrls() {
        User user = new User();
        user.setId(1);

        BadgeDefinition bronze = new BadgeDefinition();
        bronze.setId(10);
        bronze.setCode("TREE_BRONZE");
        bronze.setName("Tree Bronze Badge");
        bronze.setDescription("Find 10 unique tree discoveries.");
        bronze.setCategory(PictureCategory.TREE);
        bronze.setTier(BadgeTier.BRONZE);
        bronze.setRequiredCount(10);
        bronze.setActive(true);

        BadgeDefinition silver = new BadgeDefinition();
        silver.setId(11);
        silver.setCode("TREE_SILVER");
        silver.setName("Tree Silver Badge");
        silver.setDescription("Find 20 unique tree discoveries.");
        silver.setCategory(PictureCategory.TREE);
        silver.setTier(BadgeTier.SILVER);
        silver.setRequiredCount(20);
        silver.setActive(true);

        UserBadge unlockedBronze = new UserBadge();
        unlockedBronze.setId(100);
        unlockedBronze.setUser(user);
        unlockedBronze.setBadgeDefinition(bronze);

        when(badgeDefinitionRepository.findByActiveTrue())
                .thenReturn(List.of(bronze, silver));

        when(userBadgeRepository.findByUser(user))
                .thenReturn(List.of(unlockedBronze));

        when(userDiscoveryRepository.countByUserAndCategory(user, PictureCategory.TREE))
                .thenReturn(12L);

        when(storageClient.generateSignedReadUrl("badge-icons/TREE_BRONZE.png"))
                .thenReturn("https://signed-url.test/TREE_BRONZE.png");

        when(storageClient.generateSignedReadUrl("badge-icons/TREE_SILVER.png"))
                .thenReturn("https://signed-url.test/TREE_SILVER.png");

        List<BadgeProgressResponse> responses = badgeService.getAllBadgesForUser(user);

        assertEquals(2, responses.size());

        BadgeProgressResponse bronzeResponse = responses.get(0);
        assertEquals("TREE_BRONZE", bronzeResponse.getCode());
        assertEquals(true, bronzeResponse.isUnlocked());
        assertEquals(100, bronzeResponse.getUserBadgeId());
        assertEquals(12L, bronzeResponse.getCurrentCount());
        assertEquals("https://signed-url.test/TREE_BRONZE.png", bronzeResponse.getImageUrl());

        BadgeProgressResponse silverResponse = responses.get(1);
        assertEquals("TREE_SILVER", silverResponse.getCode());
        assertEquals(false, silverResponse.isUnlocked());
        assertEquals(null, silverResponse.getUserBadgeId());
        assertEquals(12L, silverResponse.getCurrentCount());
        assertEquals("https://signed-url.test/TREE_SILVER.png", silverResponse.getImageUrl());
    }
}