package com.lms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative URL path
        return "/uploads/" + subDirectory + "/" + uniqueFilename;
    }

    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        
        Path filePath = Paths.get(uploadDir, fileUrl.substring("/uploads/".length()));
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    public long getFileSize(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return 0;
        }
        
        Path filePath = Paths.get(uploadDir, fileUrl.substring("/uploads/".length()));
        if (Files.exists(filePath)) {
            return Files.size(filePath);
        }
        return 0;
    }
}

