package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.achievement.service.BadgeService;
import com.example.accessingdatamysql.gamification.UserProgressionService;
import com.example.accessingdatamysql.model.challenge.service.ChallengeProgressService;
import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.picture.entity.Picture;
import com.example.accessingdatamysql.picture.enums.PictureCategory;
import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private PictureService pictureService;

    @Test
    void createPicture_challengeMode_shouldAwardDiscoveryPointsAndChallengeReward() {
        User user = new User();
        user.setId(1);
        user.setTotalPoints(60);
        user.setLevel(Level.LEVEL_1);

        CreatePictureRequest request = new CreatePictureRequest();
        request.setImageUrl("https://example.com/red-clover.jpg");
        request.setTargetType(TargetType.PLANT);
        request.setPictureMode(PictureMode.CHALLENGE);

        AiIdentificationResult aiResult = mock(AiIdentificationResult.class);
        when(aiResult.getLabel()).thenReturn("Red clover");
        when(aiResult.getCategory()).thenReturn("FLOWER");
        when(aiResult.getAiConfidence()).thenReturn(0.48);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(natureAiService.identifyImage("https://example.com/red-clover.jpg", TargetType.PLANT))
                .thenReturn(aiResult);

        when(discoveryService.awardDiscoveryPoints(
                eq(user),
                eq(PictureCategory.FLOWER),
                eq("Red clover"),
                eq("https://example.com/red-clover.jpg")
        )).thenReturn(5);

        when(pictureRepository.save(any(Picture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(challengeProgressService.updateProgressFromPicture(eq(user), any(Picture.class)))
                .thenReturn(100);

        PictureResponse response = pictureService.createPicture(1, request);

        assertEquals(5, response.getPointsAwarded());
        assertEquals(PictureMode.CHALLENGE, response.getPictureMode());

        verify(discoveryService).awardDiscoveryPoints(
                user,
                PictureCategory.FLOWER,
                "Red clover",
                "https://example.com/red-clover.jpg"
        );
        verify(badgeService).checkAndUnlockCategoryBadges(user, PictureCategory.FLOWER);
        verify(challengeProgressService).updateProgressFromPicture(eq(user), any(Picture.class));
        verify(userProgressionService).applyAward(user, 105);
    }

    @Test
    void createPicture_collectionMode_withExistingDiscovery_shouldGiveZeroPointsAndSkipChallengeReward() {
        User user = new User();
        user.setId(1);
        user.setTotalPoints(25);
        user.setLevel(Level.LEVEL_1);

        CreatePictureRequest request = new CreatePictureRequest();
        request.setImageUrl("https://example.com/oak.jpg");
        request.setTargetType(TargetType.PLANT);
        request.setPictureMode(PictureMode.COLLECTION);

        AiIdentificationResult aiResult = mock(AiIdentificationResult.class);
        when(aiResult.getLabel()).thenReturn("Oak");
        when(aiResult.getCategory()).thenReturn("TREE");
        when(aiResult.getAiConfidence()).thenReturn(0.91);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(natureAiService.identifyImage("https://example.com/oak.jpg", TargetType.PLANT))
                .thenReturn(aiResult);

        when(discoveryService.awardDiscoveryPoints(
                eq(user),
                eq(PictureCategory.TREE),
                eq("Oak"),
                eq("https://example.com/oak.jpg")
        )).thenReturn(0);

        when(pictureRepository.save(any(Picture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PictureResponse response = pictureService.createPicture(1, request);

        assertEquals(0, response.getPointsAwarded());
        assertEquals(PictureMode.COLLECTION, response.getPictureMode());
        assertEquals(25, user.getTotalPoints());
        assertEquals(Level.LEVEL_1, user.getLevel());

        verify(discoveryService).awardDiscoveryPoints(
                user,
                PictureCategory.TREE,
                "Oak",
                "https://example.com/oak.jpg"
        );
        verify(challengeProgressService, never()).updateProgressFromPicture(any(), any());
        verify(badgeService, never()).checkAndUnlockCategoryBadges(any(), any());
        verify(userProgressionService).applyAward(user, 0);
        verify(userRepository, never()).save(user);
    }

    @Test
    void createPicture_collectionMode_shouldUnlockBadgeWhenNewDiscoveryGivesPoints() {
        User user = new User();
        user.setId(1);
        user.setTotalPoints(95);
        user.setLevel(Level.LEVEL_1);

        CreatePictureRequest request = new CreatePictureRequest();
        request.setImageUrl("https://example.com/birch.jpg");
        request.setTargetType(TargetType.PLANT);
        request.setPictureMode(PictureMode.COLLECTION);

        AiIdentificationResult aiResult = mock(AiIdentificationResult.class);
        when(aiResult.getLabel()).thenReturn("Birch");
        when(aiResult.getCategory()).thenReturn("TREE");
        when(aiResult.getAiConfidence()).thenReturn(0.88);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(natureAiService.identifyImage("https://example.com/birch.jpg", TargetType.PLANT))
                .thenReturn(aiResult);

        when(discoveryService.awardDiscoveryPoints(
                eq(user),
                eq(PictureCategory.TREE),
                eq("Birch"),
                eq("https://example.com/birch.jpg")
        )).thenReturn(5);

        when(pictureRepository.save(any(Picture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PictureResponse response = pictureService.createPicture(1, request);

        assertEquals(5, response.getPointsAwarded());
        assertEquals(PictureMode.COLLECTION, response.getPictureMode());
        assertEquals(95, user.getTotalPoints());
        assertEquals(Level.LEVEL_1, user.getLevel());

        verify(badgeService).checkAndUnlockCategoryBadges(user, PictureCategory.TREE);
        verify(challengeProgressService, never()).updateProgressFromPicture(any(), any());
        verify(userProgressionService).applyAward(user, 5);
        verify(userRepository, never()).save(user);
    }
}