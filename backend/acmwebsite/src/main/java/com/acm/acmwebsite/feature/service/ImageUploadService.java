package com.acm.acmwebsite.feature.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to upload an empty or null file");
            throw new IllegalArgumentException("Failed to upload image: The provided file is empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Attempted upload of non-image file type: {}", contentType);
            throw new IllegalArgumentException("Failed to upload: Only image files (e.g. PNG, JPG, JPEG, GIF) are allowed.");
        }

        log.info("Starting image upload to Cloudinary. Filename: {}, Content-Type: {}, Size: {} bytes", 
            file.getOriginalFilename(), contentType, file.getSize());

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded image to Cloudinary. Secure URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Cloudinary upload failed for filename: {}. Error: {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new IOException("Failed to upload image to Cloudinary service: " + e.getMessage(), e);
        }
    }
}
