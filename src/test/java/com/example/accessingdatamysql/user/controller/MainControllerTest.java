package com.example.accessingdatamysql.user.controller;

import com.example.accessingdatamysql.user.dto.UpdateProfileImageRequest;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MainController mainController;

    @Test
    void updateProfileImage_shouldSaveUserAndReturnMappedResponse() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "1")
                .build();

        UpdateProfileImageRequest request = new UpdateProfileImageRequest();
        request.setProfileImageUrl("https://example.com/avatar.png");

        User user = new User();
        user.setId(1);
        user.setProfileImageUrl(null);

        UserResponse mappedResponse = mock(UserResponse.class);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(mappedResponse);

        ResponseEntity<?> response = mainController.updateProfileImage(jwt, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("https://example.com/avatar.png", user.getProfileImageUrl());
        verify(userRepository).save(user);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void updateProfileImage_shouldReturnBadRequestWhenUrlIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "1")
                .build();

        UpdateProfileImageRequest request = new UpdateProfileImageRequest();
        request.setProfileImageUrl("   ");

        ResponseEntity<?> response = mainController.updateProfileImage(jwt, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("profileImageUrl is required", response.getBody());
        verify(userRepository, never()).save(any());
    }
}