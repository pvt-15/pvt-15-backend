package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.achievement.dto.BadgeResponse;
import com.example.accessingdatamysql.achievement.service.BadgeService;
import com.example.accessingdatamysql.gamification.UserProgressionService;
import com.example.accessingdatamysql.model.challenge.service.ChallengeProgressService;
import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureCreateResultResponse;
import com.example.accessingdatamysql.picture.entity.Picture;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.picture.enums.AiProvider;
import com.example.accessingdatamysql.storage.client.StorageClient;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PictureServiceTest {

    @Mock
    private PictureRepository pictureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NatureAiService natureAiService;

    @Mock
    private DiscoveryService discoveryService;

    @Mock
    private ChallengeProgressService challengeProgressService;

    @Mock
    private BadgeService badgeService;

    @Mock
    private UserProgressionService userProgressionService;

    @Mock
    private StorageClient storageClient;

    @InjectMocks
    private PictureService pictureService;

    @Test
    void createPicture_challengeMode_shouldAwardChallengeRewardOnly() {
        User user = new User();
        user.setId(1);
        user.setTotalPoints(60);
        user.setLevel(Level.LEVEL_1);

        String objectKey = "pictures/user-1/red-clover.jpg";
        String signedUrl = "https://signed-url.test/red-clover.jpg";

        CreatePictureRequest request = new CreatePictureRequest();
        request.setImageObjectKey(objectKey);
        request.setTargetType(TargetType.PLANT);
        request.setPictureMode(PictureMode.CHALLENGE);
        request.setChallengeId(22);

        AiIdentificationResult aiResult = mock(AiIdentificationResult.class);
        when(aiResult.getLabel()).thenReturn("Red clover");
        when(aiResult.getCategory()).thenReturn("FLOWER");
        when(aiResult.getAiConfidence()).thenReturn(0.88);
        when(aiResult.getAiProvider()).thenReturn(AiProvider.GOOGLE_VISION);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(storageClient.generateSignedReadUrl(objectKey)).thenReturn(signedUrl);
        when(natureAiService.identifyImage(signedUrl, TargetType.PLANT)).thenReturn(aiResult);

        when(challengeProgressService.matchesAnyTask(
                eq(user),
                eq(22),
                eq(PictureCategory.FLOWER),
                eq("Red clover")
        )).thenReturn(true);

        when(pictureRepository.save(any(Picture.class)))
                .thenAnswer(invocation -> {
                    Picture picture = invocation.getArgument(0);
                    picture.setId(100);
                    return picture;
                });

        when(challengeProgressService.updateProgressFromPicture(eq(user), any(Picture.class), eq(22)))
                .thenReturn(100);

        PictureCreateResultResponse response = pictureService.createPicture(1, request);

        assertEquals(0, response.getPicture().getPointsAwarded());
        assertEquals(PictureMode.CHALLENGE, response.getPicture().getPictureMode());
        assertEquals(signedUrl, response.getPicture().getImageUrl());
        assertFalse(response.getGamification().isLeveledUp());
        assertEquals(0, response.getGamification().getNewlyUnlockedBadges().size());

        verify(discoveryService, never()).awardDiscoveryPoints(any(), any(), any(), any());
        verify(badgeService, never()).checkAndUnlockCategoryBadges(any(), any());
        verify(challengeProgressService).matchesAnyTask(
                eq(user),
                eq(22),
                eq(PictureCategory.FLOWER),
                eq("Red clover")
        );
        verify(challengeProgressService).updateProgressFromPicture(eq(user), any(Picture.class), eq(22));
        verify(userProgressionService).applyAward(user, 100);

        ArgumentCaptor<Picture> pictureCaptor = ArgumentCaptor.forClass(Picture.class);
        verify(pictureRepository).save(pictureCaptor.capture());

        Picture savedPicture = pictureCaptor.getValue();
        assertEquals("Red clover", savedPicture.getLabel());
        assertEquals(PictureCategory.FLOWER, savedPicture.getCategory());
        assertEquals(objectKey, savedPicture.getImageObjectKey());
        assertNull(savedPicture.getImageUrl());
    }

    @Test
    void createPicture_collectionMode_withExistingDiscovery_shouldGiveZeroPointsAndSkipChallengeReward() {
        User user = new User();
        user.setId(1);
        user.setTotalPoints(25);
        user.setLevel(Level.LEVEL_1);

        String objectKey = "pictures/user-1/oak.jpg";
        String signedUrl = "https://signed-url.test/oak.jpg";

        CreatePictureRequest request = new CreatePictureRequest();
        request.setImageObjectKey(objectKey);
        request.setTargetType(TargetType.PLANT);
        request.setPictureMode(PictureMode.COLLECTION);

        AiIdentificationResult aiResult = mock(AiIdentificationResult.class);
        when(aiResult.getLabel()).thenReturn("Oak");
        when(aiResult.getCategory()).thenReturn("TREE");
        when(aiResult.getAiConfidence()).thenReturn(0.91);
        when(aiResult.getAiProvider()).thenReturn(AiProvider.GOOGLE_VISION);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(storageClient.generateSignedReadUrl(objectKey)).thenReturn(signedUrl);
        when(natureAiService.identifyImage(signedUrl, TargetType.PLANT)).thenReturn(aiResult);

        when(discoveryService.awardDiscoveryPoints(
                eq(user),
                eq(PictureCategory.TREE),
                eq("Oak"),
                eq(objectKey)
        )).thenReturn(0);

        when(pictureRepository.save(any(Picture.class)))
                .thenAnswer(invocation -> {
                    Picture picture = invocation.getArgument(0);
                    picture.setId(101);
                    return picture;
                });

        PictureCreateResultResponse response = pictureService.createPicture(1, request);

        assertEquals(0, response.getPicture().getPointsAwarded());
        assertEquals(PictureMode.COLLECTION, response.getPicture().getPictureMode());
        assertEquals(signedUrl, response.getPicture().getImageUrl());
        assertFalse(response.getGamification().isLeveledUp());
        assertEquals(0, response.getGamification().getNewlyUnlockedBadges().size());

        assertEquals(25, user.getTotalPoints());
        assertEquals(Level.LEVEL_1, user.getLevel());

        verify(natureAiService).identifyImage(signedUrl, TargetType.PLANT);
        verify(discoveryService).awardDiscoveryPoints(
                user,
                PictureCategory.TREE,
                "Oak",
                objectKey
        );

        verify(challengeProgressService, never()).updateProgressFromPicture(any(), any(), anyInt());
        verify(badgeService, never()).checkAndUnlockCategoryBadges(any(), any());
        verify(userProgressionService, never()).applyAward(any(), anyInt());
        verify(userRepository, never()).save(user);
    }

    @Test
    void createPicture_collectionMode_shouldUnlockBadgeWhenNewDiscoveryGivesPoints() {
        User user = new User();
        user.setId(1);
        user.setTotalPoints(95);
        user.setLevel(Level.LEVEL_1);

        String objectKey = "pictures/user-1/birch.jpg";
        String signedUrl = "https://signed-url.test/birch.jpg";

        CreatePictureRequest request = new CreatePictureRequest();
        request.setImageObjectKey(objectKey);
        request.setTargetType(TargetType.PLANT);
        request.setPictureMode(PictureMode.COLLECTION);

        AiIdentificationResult aiResult = mock(AiIdentificationResult.class);
        when(aiResult.getLabel()).thenReturn("Birch");
        when(aiResult.getCategory()).thenReturn("TREE");
        when(aiResult.getAiConfidence()).thenReturn(0.88);
        when(aiResult.getAiProvider()).thenReturn(AiProvider.GOOGLE_VISION);

        BadgeResponse badgeResponse = new BadgeResponse(
                1,
                "TREE_BRONZE",
                "Tree Beginner",
                "Discover 5 unique trees",
                "TREE",
                "BRONZE",
                5,
                "2026-05-13T12:00:00",
                "https://example.com/tree-badge.png"
        );

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(storageClient.generateSignedReadUrl(objectKey)).thenReturn(signedUrl);
        when(natureAiService.identifyImage(signedUrl, TargetType.PLANT)).thenReturn(aiResult);

        when(discoveryService.awardDiscoveryPoints(
                eq(user),
                eq(PictureCategory.TREE),
                eq("Birch"),
                eq(objectKey)
        )).thenReturn(5);

        when(pictureRepository.save(any(Picture.class)))
                .thenAnswer(invocation -> {
                    Picture picture = invocation.getArgument(0);
                    picture.setId(102);
                    return picture;
                });

        when(badgeService.checkAndUnlockCategoryBadges(user, PictureCategory.TREE))
                .thenReturn(List.of(badgeResponse));

        PictureCreateResultResponse response = pictureService.createPicture(1, request);

        assertEquals(5, response.getPicture().getPointsAwarded());
        assertEquals(PictureMode.COLLECTION, response.getPicture().getPictureMode());
        assertEquals(signedUrl, response.getPicture().getImageUrl());
        assertEquals(1, response.getGamification().getNewlyUnlockedBadges().size());
        assertEquals("TREE_BRONZE",
                response.getGamification().getNewlyUnlockedBadges().get(0).getCode());

        verify(natureAiService).identifyImage(signedUrl, TargetType.PLANT);
        verify(discoveryService).awardDiscoveryPoints(
                user,
                PictureCategory.TREE,
                "Birch",
                objectKey
        );
        verify(badgeService).checkAndUnlockCategoryBadges(user, PictureCategory.TREE);
        verify(challengeProgressService, never()).updateProgressFromPicture(any(), any(), anyInt());
        verify(userProgressionService).applyAward(user, 5);
        verify(userRepository, never()).save(user);
    }
}