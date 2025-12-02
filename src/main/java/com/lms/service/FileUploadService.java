package com.lms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileUploadService {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Use cloud storage service (S3 or local based on configuration)
        return cloudStorageService.uploadFile(file, subDirectory);
    }

    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null) {
            return;
        }

        // Use cloud storage service for deletion
        cloudStorageService.deleteFile(fileUrl);
    }

    public long getFileSize(String fileUrl) throws IOException {
        if (fileUrl == null) {
            return 0;
        }

        // For S3, we can't easily get file size without making a request
        // For local files, use the existing logic
        if (!cloudStorageService.isS3Enabled() && fileUrl.startsWith("/uploads/")) {
            Path filePath = Paths.get(uploadDir, fileUrl.substring("/uploads/".length()));
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
        }
        return 0;
    }
}

