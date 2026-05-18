package com.example.accessingdatamysql.gamification;

import com.example.accessingdatamysql.user.enums.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelTest {

    @Test
    void fromPoints_shouldReturnCorrectLevel() {
        assertEquals(Level.LEVEL_1, Level.fromPoints(0));
        assertEquals(Level.LEVEL_1, Level.fromPoints(149));
        assertEquals(Level.LEVEL_2, Level.fromPoints(150));
        assertEquals(Level.LEVEL_3, Level.fromPoints(300));
        assertEquals(Level.LEVEL_4, Level.fromPoints(600));
    }
}
