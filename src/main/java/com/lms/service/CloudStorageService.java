package com.lms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class CloudStorageService {

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.bucket-name:lms-uploads}")
    private String bucketName;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    @Value("${app.upload.dir:uploads}")
    private String localUploadDir;

    private S3Client s3Client;

    @Autowired
    public CloudStorageService() {
        // S3Client will be initialized lazily if S3 is enabled
    }

    private S3Client getS3Client() {
        if (s3Client == null && s3Enabled) {
            try {
                AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
                
                // Build S3 client with credentials and region
                var builder = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCreds));

                // Support for S3-compatible services (e.g., DigitalOcean Spaces, MinIO)
                if (endpoint != null && !endpoint.isEmpty()) {
                    builder.endpointOverride(java.net.URI.create(endpoint));
                }

                s3Client = builder.build();
                log.info("S3 client initialized for bucket: {} in region: {}", bucketName, region);
            } catch (Exception e) {
                log.error("Failed to initialize S3 client: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize S3 client", e);
            }
        }
        return s3Client;
    }

    /**
     * Upload file to cloud storage (S3) or local storage based on configuration
     */
    public String uploadFile(MultipartFile file, String subdirectory) throws IOException {
        if (s3Enabled) {
            return uploadToS3(file, subdirectory);
        } else {
            return uploadToLocal(file, subdirectory);
        }
    }

    /**
     * Upload file to AWS S3
     */
    private String uploadToS3(MultipartFile file, String subdirectory) throws IOException {
        S3Client client = getS3Client();
        if (client == null) {
            throw new RuntimeException("S3 client not initialized");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String s3Key = subdirectory != null && !subdirectory.isEmpty() 
                ? subdirectory + "/" + uniqueFileName 
                : uniqueFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Generate public URL
            String fileUrl = generateS3Url(s3Key);
            log.info("File uploaded to S3: {} -> {}", s3Key, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload file to local storage
     */
    private String uploadToLocal(MultipartFile file, String subdirectory) throws IOException {
        Path uploadPath = Paths.get(localUploadDir);
        if (subdirectory != null && !subdirectory.isEmpty()) {
            uploadPath = uploadPath.resolve(subdirectory);
        }

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        String fileUrl = "/uploads/" + (subdirectory != null && !subdirectory.isEmpty() ? subdirectory + "/" : "") + uniqueFileName;
        log.info("File uploaded locally: {}", fileUrl);
        return fileUrl;
    }

    /**
     * Delete file from cloud storage or local storage
     */
    public void deleteFile(String fileUrl) {
        if (s3Enabled) {
            deleteFromS3(fileUrl);
        } else {
            deleteFromLocal(fileUrl);
        }
    }

    /**
     * Delete file from S3
     */
    private void deleteFromS3(String fileUrl) {
        S3Client client = getS3Client();
        if (client == null) {
            log.warn("S3 client not initialized, cannot delete file: {}", fileUrl);
            return;
        }

        try {
            // Extract S3 key from URL
            String s3Key = extractS3KeyFromUrl(fileUrl);
            if (s3Key == null) {
                log.warn("Could not extract S3 key from URL: {}", fileUrl);
                return;
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            client.deleteObject(deleteRequest);
            log.info("File deleted from S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete file from local storage
     */
    private void deleteFromLocal(String fileUrl) {
        try {
            // Remove /uploads/ prefix if present
            String relativePath = fileUrl.startsWith("/uploads/") 
                    ? fileUrl.substring("/uploads/".length()) 
                    : fileUrl;
            Path filePath = Paths.get(localUploadDir).resolve(relativePath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted locally: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Error deleting local file: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate S3 public URL
     */
    private String generateS3Url(String s3Key) {
        if (endpoint != null && !endpoint.isEmpty()) {
            // For S3-compatible services
            return endpoint + "/" + bucketName + "/" + s3Key;
        } else {
            // Standard S3 URL format
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
        }
    }

    /**
     * Extract S3 key from URL
     */
    private String extractS3KeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Handle S3-compatible endpoint URLs
        if (endpoint != null && !endpoint.isEmpty() && url.startsWith(endpoint)) {
            String path = url.substring(endpoint.length());
            if (path.startsWith("/" + bucketName + "/")) {
                return path.substring(("/" + bucketName + "/").length());
            }
        }

        // Handle standard S3 URLs
        String s3Pattern = bucketName + ".s3." + region + ".amazonaws.com/";
        if (url.contains(s3Pattern)) {
            return url.substring(url.indexOf(s3Pattern) + s3Pattern.length());
        }

        // Handle URLs with bucket name in path
        String bucketPattern = "/" + bucketName + "/";
        if (url.contains(bucketPattern)) {
            return url.substring(url.indexOf(bucketPattern) + bucketPattern.length());
        }

        return null;
    }

    /**
     * Check if S3 is enabled
     */
    public boolean isS3Enabled() {
        return s3Enabled;
    }
}

