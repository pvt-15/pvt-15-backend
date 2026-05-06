package com.example.accessingdatamysql.storage.service;

import com.example.accessingdatamysql.storage.config.StorageProperties;
import com.example.accessingdatamysql.storage.dto.ImageUploadResponse;
import com.example.accessingdatamysql.storage.enums.StorageFolder;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcsImageStorageServiceTest {

    private Storage storage;
    private StorageProperties storageProperties;
    private GcsImageStorageService gcsImageStorageService;

    @BeforeEach
    void setUp() {
        storage = mock(Storage.class);
        storageProperties = mock(StorageProperties.class);
        gcsImageStorageService = new GcsImageStorageService(storage, storageProperties);
    }

    @Test
    void uploadImage_shouldUploadToGcsAndReturnResponse() throws Exception {
        when(storageProperties.getBucketName()).thenReturn("my-test-bucket");

        URL signedUrl = new URL("https://signed-url.test/flower.jpg");
        when(storage.signUrl(
                any(BlobInfo.class),
                anyLong(),
                any(TimeUnit.class),
                any(Storage.SignUrlOption.class)
        )).thenReturn(signedUrl);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "flower.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        ImageUploadResponse response = gcsImageStorageService.uploadImage(
                file,
                StorageFolder.PICTURES,
                42
        );

        assertNotNull(response);
        assertEquals("https://signed-url.test/flower.jpg", response.getImageUrl());
        assertNotNull(response.getObjectKey());

        assertTrue(response.getObjectKey().startsWith("pictures/user-42/"));
        assertTrue(response.getObjectKey().endsWith(".jpg"));

        ArgumentCaptor<BlobInfo> blobInfoCaptor = ArgumentCaptor.forClass(BlobInfo.class);
        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);

        verify(storage).create(blobInfoCaptor.capture(), bytesCaptor.capture());

        BlobInfo uploadedBlobInfo = blobInfoCaptor.getValue();

        assertEquals("my-test-bucket", uploadedBlobInfo.getBlobId().getBucket());
        assertEquals(response.getObjectKey(), uploadedBlobInfo.getBlobId().getName());
        assertEquals("image/jpeg", uploadedBlobInfo.getContentType());
        assertArrayEquals("fake-image-content".getBytes(), bytesCaptor.getValue());

        verify(storage).signUrl(
                any(BlobInfo.class),
                anyLong(),
                any(TimeUnit.class),
                any(Storage.SignUrlOption.class)
        );
    }

    @Test
    void uploadImage_shouldUseDefaultJpgExtensionWhenFilenameHasNoExtension() throws Exception {
        when(storageProperties.getBucketName()).thenReturn("my-test-bucket");

        URL signedUrl = new URL("https://signed-url.test/default.jpg");
        when(storage.signUrl(
                any(BlobInfo.class),
                anyLong(),
                any(TimeUnit.class),
                any(Storage.SignUrlOption.class)
        )).thenReturn(signedUrl);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "flower",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        ImageUploadResponse response = gcsImageStorageService.uploadImage(
                file,
                StorageFolder.PROFILE_IMAGES,
                7
        );

        assertNotNull(response);
        assertEquals("https://signed-url.test/default.jpg", response.getImageUrl());
        assertNotNull(response.getObjectKey());

        assertTrue(response.getObjectKey().startsWith("profile-images/user-7/"));
        assertTrue(response.getObjectKey().endsWith(".jpg"));

        verify(storage).create(any(BlobInfo.class), any(byte[].class));
        verify(storage).signUrl(
                any(BlobInfo.class),
                anyLong(),
                any(TimeUnit.class),
                any(Storage.SignUrlOption.class)
        );
    }

    @Test
    void uploadImage_shouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gcsImageStorageService.uploadImage(emptyFile, StorageFolder.PICTURES, 1)
        );

        assertEquals("File is required", exception.getMessage());

        verify(storage, never()).create(any(BlobInfo.class), any(byte[].class));
        verify(storage, never()).signUrl(
                any(BlobInfo.class),
                anyLong(),
                any(TimeUnit.class),
                any(Storage.SignUrlOption.class)
        );
    }
}