package com.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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
    }
}

