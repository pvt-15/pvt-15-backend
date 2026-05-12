package com.example.accessingdatamysql.user.mapper;

import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.enums.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserMapperTest {

    private ImageStorageService imageStorageService;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        imageStorageService = mock(ImageStorageService.class);
        userMapper = new UserMapper(imageStorageService);
    }

    @Test
    void toUserResponse_shouldMapAllFieldsAndUseFallbackProfileImageUrl() {
        User user = new User();
        user.setId(1);
        user.setName("TestName");
        user.setEmail("Test.Name@example.com");
        user.setProvider(Provider.GOOGLE);
        user.setProviderUserId("google-123");
        user.setTotalPoints(120);
        user.setLevel(Level.LEVEL_2);
        user.setProfileImageUrl("https://old-url.test/profile.png");

        UserResponse response = userMapper.toUserResponse(user);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("TestName", response.getName());
        assertEquals("Test.Name@example.com", response.getEmail());
        assertEquals(Provider.GOOGLE, response.getProvider());
        assertEquals("google-123", response.getProviderUserId());
        assertEquals(120, response.getTotalPoints());
        assertEquals(Level.LEVEL_2, response.getLevel());
        assertEquals("https://old-url.test/profile.png", response.getProfileImageUrl());

        verifyNoInteractions(imageStorageService);
    }

    @Test
    void toUserResponse_shouldGenerateSignedUrlWhenProfileImageObjectKeyExists() {
        User user = new User();
        user.setId(1);
        user.setName("TestName");
        user.setEmail("test@example.com");
        user.setProvider(Provider.LOCAL);
        user.setTotalPoints(0);
        user.setLevel(Level.LEVEL_1);
        user.setProfileImageObjectKey("profile-images/presets/fox.png");

        when(imageStorageService.generateSignedReadUrl("profile-images/presets/fox.png"))
                .thenReturn("https://signed-url.test/fox.png");

        UserResponse response = userMapper.toUserResponse(user);

        assertNotNull(response);
        assertEquals("https://signed-url.test/fox.png", response.getProfileImageUrl());

        verify(imageStorageService).generateSignedReadUrl("profile-images/presets/fox.png");
    }

    @Test
    void toUserResponse_shouldReturnNullWhenUserIsNull() {
        UserResponse response = userMapper.toUserResponse(null);

        assertNull(response);
        verifyNoInteractions(imageStorageService);
    }
}