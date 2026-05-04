package com.example.accessingdatamysql.storage.controller;

import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageUploadController.class)
class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void uploadProfileImage_shouldReturnImageUploadResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        ImageUploadResponse response = new ImageUploadResponse(
                "https://storage.googleapis.com/my-bucket/profile-images/user-1/avatar.png",
                "profile-images/user-1/avatar.png"
        );

        when(imageStorageService.uploadImage(any(), eq(StorageFolder.PROFILE_IMAGES), eq(1)))
                .thenReturn(response);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "1")
                .build();

        mockMvc.perform(
                        multipart("/uploads/profile-image")
                                .file(file)
                                .with(authentication(new JwtAuthenticationToken(jwt)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://storage.googleapis.com/my-bucket/profile-images/user-1/avatar.png"))
                .andExpect(jsonPath("$.objectKey")
                        .value("profile-images/user-1/avatar.png"));

        verify(imageStorageService).uploadImage(any(), eq(StorageFolder.PROFILE_IMAGES), eq(1));
    }

    @Test
    void uploadPicture_shouldReturnImageUploadResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "flower.jpg",
                "image/jpeg",
                "fake-picture-content".getBytes()
        );

        ImageUploadResponse response = new ImageUploadResponse(
                "https://storage.googleapis.com/my-bucket/pictures/user-1/flower.jpg",
                "pictures/user-1/flower.jpg"
        );

        when(imageStorageService.uploadImage(any(), eq(StorageFolder.PICTURES), eq(1)))
                .thenReturn(response);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "1")
                .build();

        mockMvc.perform(
                        multipart("/uploads/picture")
                                .file(file)
                                .with(authentication(new JwtAuthenticationToken(jwt)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://storage.googleapis.com/my-bucket/pictures/user-1/flower.jpg"))
                .andExpect(jsonPath("$.objectKey")
                        .value("pictures/user-1/flower.jpg"));

        verify(imageStorageService).uploadImage(any(), eq(StorageFolder.PICTURES), eq(1));
    }

    @Test
    void uploadChallengeImage_shouldReturnImageUploadResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "challenge.png",
                "image/png",
                "fake-challenge-content".getBytes()
        );

        ImageUploadResponse response = new ImageUploadResponse(
                "https://storage.googleapis.com/my-bucket/challenge-images/user-1/challenge.png",
                "challenge-images/user-1/challenge.png"
        );

        when(imageStorageService.uploadImage(any(), eq(StorageFolder.CHALLENGE_IMAGES), eq(1)))
                .thenReturn(response);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "1")
                .build();

        mockMvc.perform(
                        multipart("/uploads/challenge-image")
                                .file(file)
                                .with(authentication(new JwtAuthenticationToken(jwt)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://storage.googleapis.com/my-bucket/challenge-images/user-1/challenge.png"))
                .andExpect(jsonPath("$.objectKey")
                        .value("challenge-images/user-1/challenge.png"));

        verify(imageStorageService).uploadImage(any(), eq(StorageFolder.CHALLENGE_IMAGES), eq(1));
    }
}