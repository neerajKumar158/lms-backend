package com.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Handles file upload configuration and static resource serving. This configuration
 * manages upload directory creation, static file serving from the uploads directory,
 * and resource handler setup for file access in the LMS platform.
 *
 * @author VisionWaves
 * @version 1.0
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    /**
     * Upload directory path from application properties
     */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Configures resource handlers for serving uploaded files and creates upload directory if needed.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Create uploads directory if it doesn't exist
        File uploadDirectory = Paths.get(uploadDir).toFile();
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        
        // Serve files from uploads directory
        String uploadPath = uploadDirectory.getAbsolutePath();
        if (!uploadPath.endsWith(File.separator)) {
            uploadPath += File.separator;
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(3600); // Cache for 1 hour
        
        // Create messages upload directory if it doesn't exist
        File messagesDir = Paths.get("uploads/messages").toFile();
        if (!messagesDir.exists()) {
            messagesDir.mkdirs();
        }
    }
}

