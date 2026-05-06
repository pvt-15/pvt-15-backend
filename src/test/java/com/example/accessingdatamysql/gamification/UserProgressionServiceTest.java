package com.example.accessingdatamysql.gamification;

import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserProgressionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProgressionService userProgressionService;

    @Test
    void applyAward_shouldAddPointsAndUpdateLevel() {
        User user = new User();
        user.setTotalPoints(140);
        user.setLevel(Level.LEVEL_1);

        userProgressionService.applyAward(user, 10);

        assertEquals(150, user.getTotalPoints());
        assertEquals(Level.LEVEL_2, user.getLevel());
        verify(userRepository).save(user);
    }

    @Test
    void applyAward_shouldRejectNegativePoints() {
        User user = new User();

        assertThrows(IllegalArgumentException.class, () ->
                userProgressionService.applyAward(user, -1)
        );

        verify(userRepository, never()).save(any());
    }
}