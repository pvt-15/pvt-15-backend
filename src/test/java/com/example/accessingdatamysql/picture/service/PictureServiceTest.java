package com.example.accessingdatamysql.picture.service;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.enums.PictureCategory;
import com.example.accessingdatamysql.picture.dto.AiIdentificationResult;
import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.picture.dto.PictureStatsResponse;
import com.example.accessingdatamysql.picture.entity.Picture;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import com.example.accessingdatamysql.picture.repository.PictureRepository;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PictureService.
 * Checks picture creation, point updates, level changes, deletion, and statistics
 */
@ExtendWith(MockitoExtension.class)
class PictureServiceTest {

    @Mock
    private PictureRepository pictureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NatureAiService natureAiService;

    @InjectMocks
    private PictureService pictureService;

    @Test
    void createPicture_shouldAwardPointsAndRaiseLevel() {
        Integer userId = 1;

        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("TestUser@example.com");
        user.setTotalPoints(140);
        user.setLevel(Level.LEVEL_1);

        CreatePictureRequest request =
                new CreatePictureRequest("https://example.com/tree.jpg", TargetType.PLANT);

        AiIdentificationResult aiResult =
                new AiIdentificationResult("Oak", "TREE", 0.91);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(natureAiService.identifyImage("https://example.com/tree.jpg", TargetType.PLANT))
                .thenReturn(aiResult);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pictureRepository.save(any(Picture.class))).thenAnswer(invocation -> {
            Picture savedPicture = invocation.getArgument(0);
            savedPicture.setId(100);
            return savedPicture;
        });

        PictureResponse response = pictureService.createPicture(userId, request);

        assertNotNull(response);
        assertEquals(100, response.getId());
        assertEquals("Oak", response.getLabel());
        assertEquals("TREE", response.getCategory());
        assertEquals(0.91, response.getAiConfidence(), 0.0001);
        assertEquals(20, response.getPointsAwarded());
        assertEquals("https://example.com/tree.jpg", response.getImageUrl());
        assertNotNull(response.getCreatedAt());

        assertEquals(160, user.getTotalPoints());
        assertEquals(Level.LEVEL_2, user.getLevel());

        ArgumentCaptor<Picture> pictureCaptor = ArgumentCaptor.forClass(Picture.class);
        verify(pictureRepository).save(pictureCaptor.capture());

        Picture savedPicture = pictureCaptor.getValue();
        assertEquals("Oak", savedPicture.getLabel());
        assertEquals(PictureCategory.TREE, savedPicture.getCategory());
        assertEquals(20, savedPicture.getPointsAwarded());
        assertEquals(user, savedPicture.getUser());
        assertNotNull(savedPicture.getTakenAt());
    }

    @Test
    void deletePicture_shouldFloorPointsAtZeroAndDeletePicture() {
        Integer userId = 1;
        Integer pictureId = 10;

        User user = new User();
        user.setId(userId);
        user.setTotalPoints(10);
        user.setLevel(Level.LEVEL_1);

        Picture picture = new Picture();
        picture.setId(pictureId);
        picture.setPointsAwarded(20);
        picture.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pictureRepository.findById(pictureId)).thenReturn(Optional.of(picture));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pictureService.deletePicture(userId, pictureId);

        assertEquals(0, user.getTotalPoints());
        assertEquals(Level.LEVEL_1, user.getLevel());

        verify(userRepository).save(user);
        verify(pictureRepository).delete(picture);
    }

    @Test
    void getPictureStats_shouldAggregateSpecialCategoriesCorrectly() {
        Integer userId = 1;

        User user = new User();
        user.setId(userId);

        Picture flower = new Picture();
        flower.setCategory(PictureCategory.FLOWER);
        flower.setPointsAwarded(10);

        Picture tree = new Picture();
        tree.setCategory(PictureCategory.TREE);
        tree.setPointsAwarded(15);

        Picture bird = new Picture();
        bird.setCategory(PictureCategory.BIRD);
        bird.setPointsAwarded(20);

        Picture insect = new Picture();
        insect.setCategory(PictureCategory.INSECT);
        insect.setPointsAwarded(5);

        Picture plant = new Picture();
        plant.setCategory(PictureCategory.PLANT);
        plant.setPointsAwarded(7);

        Picture animal = new Picture();
        animal.setCategory(PictureCategory.ANIMAL);
        animal.setPointsAwarded(3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pictureRepository.findByUser(user)).thenReturn(
                List.of(flower, tree, bird, insect, plant, animal)
        );

        PictureStatsResponse stats = pictureService.getPictureStats(userId);

        assertEquals(6, stats.getTotalPictures());
        assertEquals(3, stats.getTotalPlants());   // flower + tree + plant
        assertEquals(3, stats.getTotalAnimals());  // bird + insect + animal
        assertEquals(1, stats.getTotalFlowers());
        assertEquals(1, stats.getTotalTrees());
        assertEquals(1, stats.getTotalBirds());
        assertEquals(1, stats.getTotalInsects());
        assertEquals(60, stats.getTotalPoints());
    }
}