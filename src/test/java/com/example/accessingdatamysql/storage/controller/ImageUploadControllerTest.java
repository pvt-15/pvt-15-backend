package com.example.accessingdatamysql.storage.controller;

import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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

        mockMvc.perform(
                        multipart("/uploads/profile-image")
                                .file(file)
                                .with(jwt().jwt(jwt -> jwt.subject("1")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://storage.googleapis.com/my-bucket/profile-images/user-1/avatar.png"))
                .andExpect(jsonPath("$.objectKey")
                        .value("profile-images/user-1/avatar.png"));

        verify(imageStorageService).uploadImage(any(), eq(StorageFolder.PROFILE_IMAGES), eq(1));
    }
}