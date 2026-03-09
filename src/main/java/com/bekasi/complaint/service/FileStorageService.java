package com.bekasi.complaint.service;

import com.bekasi.complaint.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private static final long MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDirPath) {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + e.getMessage(), e);
        }
    }

    /**
     * Stores an image file and returns its relative path.
     * Validates that:
     * - File is not empty
     * - File is an image (by content type)
     * - File size is less than 2 MB
     */
    public String storeImage(MultipartFile file) {
        validateImage(file);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "image"
        );
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID() + fileExtension;

        try {
            Path targetLocation = this.uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored image: {}", newFilename);
            return newFilename;
        } catch (IOException e) {
            log.error("Failed to store image: {}", e.getMessage());
            throw new BadRequestException("Failed to store image file. Please try again.");
        }
    }

    /**
     * Deletes a stored image file.
     */
    public void deleteImage(String filename) {
        try {
            Path filePath = this.uploadDir.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted image: {}", filename);
        } catch (IOException e) {
            log.warn("Failed to delete image {}: {}", filename, e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required and cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file type. Only JPEG, PNG, WebP, and GIF images are allowed.");
        }

        if (file.getSize() >= MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException(
                    String.format("Image size must be less than 2 MB. Uploaded file size: %.2f MB",
                            file.getSize() / (1024.0 * 1024.0)));
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex) : ".jpg";
    }
}
