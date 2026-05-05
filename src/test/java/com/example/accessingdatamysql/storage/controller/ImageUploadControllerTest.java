package com.example.accessingdatamysql.storage.controller;

import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import com.example.accessingdatamysql.storage.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadControllerTest {

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ImageUploadController imageUploadController;

    @Test
    void uploadProfileImage_shouldReturnImageUploadResponse() {
        Jwt jwt = createJwt("1");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        ImageUploadResponse uploadResponse = new ImageUploadResponse(
                "https://storage.googleapis.com/my-bucket/profile-images/user-1/avatar.png",
                "profile-images/user-1/avatar.png"
        );

        when(imageStorageService.uploadImage(file, StorageFolder.PROFILE_IMAGES, 1))
                .thenReturn(uploadResponse);

        ResponseEntity<ImageUploadResponse> response =
                (ResponseEntity<ImageUploadResponse>) imageUploadController.uploadProfileImage(jwt, file);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                "https://storage.googleapis.com/my-bucket/profile-images/user-1/avatar.png",
                response.getBody().getImageUrl()
        );
        assertEquals(
                "profile-images/user-1/avatar.png",
                response.getBody().getObjectKey()
        );

        verify(imageStorageService).uploadImage(file, StorageFolder.PROFILE_IMAGES, 1);
    }

    @Test
    void uploadPicture_shouldReturnImageUploadResponse() {
        Jwt jwt = createJwt("1");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "flower.jpg",
                "image/jpeg",
                "fake-picture-content".getBytes()
        );

        ImageUploadResponse uploadResponse = new ImageUploadResponse(
                "https://storage.googleapis.com/my-bucket/pictures/user-1/flower.jpg",
                "pictures/user-1/flower.jpg"
        );

        when(imageStorageService.uploadImage(file, StorageFolder.PICTURES, 1))
                .thenReturn(uploadResponse);

        ResponseEntity<ImageUploadResponse> response =
                imageUploadController.uploadPicture(jwt, file);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                "https://storage.googleapis.com/my-bucket/pictures/user-1/flower.jpg",
                response.getBody().getImageUrl()
        );
        assertEquals(
                "pictures/user-1/flower.jpg",
                response.getBody().getObjectKey()
        );

        verify(imageStorageService).uploadImage(file, StorageFolder.PICTURES, 1);
    }

    @Test
    void uploadChallengeImage_shouldReturnImageUploadResponse() {
        Jwt jwt = createJwt("1");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "challenge.png",
                "image/png",
                "fake-challenge-content".getBytes()
        );

        ImageUploadResponse uploadResponse = new ImageUploadResponse(
                "https://storage.googleapis.com/my-bucket/challenge-images/user-1/challenge.png",
                "challenge-images/user-1/challenge.png"
        );

        when(imageStorageService.uploadImage(file, StorageFolder.CHALLENGE_IMAGES, 1))
                .thenReturn(uploadResponse);

        ResponseEntity<ImageUploadResponse> response =
                imageUploadController.uploadChallengeImage(jwt, file);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                "https://storage.googleapis.com/my-bucket/challenge-images/user-1/challenge.png",
                response.getBody().getImageUrl()
        );
        assertEquals(
                "challenge-images/user-1/challenge.png",
                response.getBody().getObjectKey()
        );

        verify(imageStorageService).uploadImage(file, StorageFolder.CHALLENGE_IMAGES, 1);
    }

    private Jwt createJwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", subject)
                .build();
    }
}