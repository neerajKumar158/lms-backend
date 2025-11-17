package com.lms.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/lms/files")
public class FileDownloadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename) {
        try {
            // Sanitize filename to prevent directory traversal
            String sanitizedFilename = filename.replaceAll("\\.\\.", "").replaceAll("/", "");
            
            Path filePath = Paths.get(uploadDir, sanitizedFilename);
            File file = filePath.toFile();
            
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if file is within upload directory (security check)
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            if (!resolvedPath.startsWith(uploadPath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Resource resource = new FileSystemResource(file);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}



