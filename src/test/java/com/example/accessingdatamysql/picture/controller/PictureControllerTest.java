package com.example.accessingdatamysql.picture.controller;

import com.example.accessingdatamysql.config.SecurityConfig;
import com.example.accessingdatamysql.picture.dto.CreatePictureRequest;
import com.example.accessingdatamysql.picture.dto.PictureResponse;
import com.example.accessingdatamysql.picture.model.enums.TargetType;
import com.example.accessingdatamysql.picture.service.PictureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for PictureController.
 */
@WebMvcTest(PictureController.class)
@Import(SecurityConfig.class)
class PictureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PictureService pictureService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createPicture_shouldReturnOkForAuthenticatedUser() throws Exception {
        CreatePictureRequest request = new CreatePictureRequest(
                "https://example.com/tree.jpg",
                TargetType.PLANT
        );

        PictureResponse response = new PictureResponse(
                100,
                "Oak",
                "TREE",
                0.91,
                20,
                "https://example.com/tree.jpg",
                "2026-04-23T09:00:00"
        );

        when(pictureService.createPicture(eq(42), any(CreatePictureRequest.class)))
                .thenReturn(response);

        Jwt testJwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("42")
                .build();

        mockMvc.perform(post("/pictures")
                        .with(jwt().jwt(testJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.label").value("Oak"))
                .andExpect(jsonPath("$.category").value("TREE"))
                .andExpect(jsonPath("$.aiConfidence").value(0.91))
                .andExpect(jsonPath("$.pointsAwarded").value(20))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/tree.jpg"))
                .andExpect(jsonPath("$.createdAt").value("2026-04-23T09:00:00"));

        verify(pictureService).createPicture(eq(42), any(CreatePictureRequest.class));
    }
}