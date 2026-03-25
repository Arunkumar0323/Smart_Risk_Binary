package com.TheSecureSyndicate.threat_detection_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path uploadDir = Paths.get("uploads");

    /**
     * Saves a file from raw byte array to the uploads directory.
     */
    public Path saveTemp(byte[] fileBytes, String fileName, String id) throws IOException {
        Files.createDirectories(uploadDir);
        Path filePath = uploadDir.resolve(id + "_" + sanitize(fileName));
        Files.write(filePath, fileBytes);
        logger.info("📁 File saved: {}", filePath.toAbsolutePath());
        return filePath;
    }

    /**
     * Saves a MultipartFile to the uploads directory.
     */
    public Path saveTemp(MultipartFile file, String id) throws IOException {
        Files.createDirectories(uploadDir);
        String originalName = sanitize(file.getOriginalFilename());
        Path filePath = uploadDir.resolve(id + "_" + originalName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("📁 Multipart file saved: {}", filePath.toAbsolutePath());
        return filePath;
    }

    /**
     * Sanitizes file names to prevent path traversal or unsafe characters.
     */
    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }
}
