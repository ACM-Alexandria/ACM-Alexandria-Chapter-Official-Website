package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.service.ImageUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("uploadImage - throws IllegalArgumentException when file is null")
    void uploadImage_nullFile_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageUploadService.uploadImage(null));
        assertEquals("Failed to upload image: The provided file is empty.", ex.getMessage());
        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("uploadImage - throws IllegalArgumentException when file is empty")
    void uploadImage_emptyFile_throws() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageUploadService.uploadImage(emptyFile));
        assertEquals("Failed to upload image: The provided file is empty.", ex.getMessage());
        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("uploadImage - throws IllegalArgumentException when file size exceeds 5MB")
    void uploadImage_fileExceeds5MB_throws() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.png", "image/png", largeContent
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageUploadService.uploadImage(largeFile));
        assertTrue(ex.getMessage().contains("File exceeds the maximum allowed size of 5 MB"));
        assertTrue(ex.getMessage().contains("actual size: 6.00 MB"));
        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("uploadImage - throws IllegalArgumentException when contentType is null")
    void uploadImage_nullContentType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.bin", null, "content".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageUploadService.uploadImage(file));
        assertEquals("Failed to upload: Only image files (e.g. PNG, JPG, JPEG, GIF) are allowed.", ex.getMessage());
        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("uploadImage - throws IllegalArgumentException when contentType does not start with image/")
    void uploadImage_nonImageContentType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "content".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageUploadService.uploadImage(file));
        assertEquals("Failed to upload: Only image files (e.g. PNG, JPG, JPEG, GIF) are allowed.", ex.getMessage());
        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("uploadImage - successfully uploads image and returns secure URL")
    void uploadImage_success() throws IOException {
        byte[] content = "valid image bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", content
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(content), eq(ObjectUtils.emptyMap())))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/sample.jpg"));

        String url = imageUploadService.uploadImage(file);

        assertNotNull(url);
        assertEquals("https://res.cloudinary.com/demo/image/upload/sample.jpg", url);
        verify(cloudinary).uploader();
        verify(uploader).upload(eq(content), eq(ObjectUtils.emptyMap()));
    }

    @Test
    @DisplayName("uploadImage - throws IOException when Cloudinary uploader throws Exception")
    void uploadImage_cloudinaryFails_throwsIOException() throws IOException {
        byte[] content = "image bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", content
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any())).thenThrow(new RuntimeException("Cloudinary service unavailable"));

        IOException ex = assertThrows(IOException.class,
                () -> imageUploadService.uploadImage(file));
        assertTrue(ex.getMessage().contains("Failed to upload image to Cloudinary service: Cloudinary service unavailable"));
        assertInstanceOf(RuntimeException.class, ex.getCause());
    }
}
