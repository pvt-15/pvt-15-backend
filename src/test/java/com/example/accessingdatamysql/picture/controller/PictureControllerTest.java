package com.example.accessingdatamysql.picture.controller;

import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.picture.enums.PictureMode;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import com.example.accessingdatamysql.picture.service.PictureService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class PictureControllerTest {

    private final PictureService pictureService = mock(PictureService.class);
    private final PictureController pictureController = new PictureController(pictureService);

    @Test
    void createPicture_shouldReturnOkForAuthenticatedUser() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("42");

        CreatePictureRequest request = new CreatePictureRequest(
                "https://example.com/tree.jpg",
                TargetType.PLANT,
                PictureMode.COLLECTION
        );

        PictureResponse response = new PictureResponse(
                100,
                "Oak",
                "TREE",
                0.91,
                20,
                "https://example.com/tree.jpg",
                "2026-04-23T09:00:00",
                PictureMode.COLLECTION
        );

        when(pictureService.createPicture(42, request)).thenReturn(response);

        ResponseEntity<?> result = pictureController.createPicture(jwt, request);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());

        verify(pictureService).createPicture(42, request);
    }
}