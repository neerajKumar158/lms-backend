package com.lms.web;

import com.lms.domain.StudyMaterial;
import com.lms.repository.LectureRepository;
import com.lms.service.CourseService;
import com.lms.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/lms/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private LectureRepository lectureRepository;

    @PostMapping("/material/{lectureId}")
    public ResponseEntity<Map<String, Object>> uploadStudyMaterial(
            @AuthenticationPrincipal User principal,
            @PathVariable("lectureId") Long lectureId,
            @RequestBody(required = false) Map<String, Object> materialData,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "materialType", required = false) String materialType,
            @RequestParam(value = "isFree", defaultValue = "false") Boolean isFree) {
        try {
            lectureRepository.findById(lectureId)
                    .orElseThrow(() -> new RuntimeException("Lecture not found"));

            String fileUrl = null;
            long fileSize = 0;
            String fileName = null;

            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                fileUrl = fileUploadService.uploadFile(file, "materials");
                fileSize = fileUploadService.getFileSize(fileUrl);
                fileName = file.getOriginalFilename();
            } else if (materialData != null && materialData.containsKey("fileUrl")) {
                // File already uploaded via /upload endpoint
                fileUrl = (String) materialData.get("fileUrl");
                fileSize = materialData.containsKey("fileSize") ? ((Number) materialData.get("fileSize")).longValue() : 0;
                fileName = materialData.containsKey("fileName") ? (String) materialData.get("fileName") : null;
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "File is required"));
            }

            // Get title and type from request body or parameters
            String materialTitle = materialData != null && materialData.containsKey("title") ? 
                    (String) materialData.get("title") : title;
            String materialTypeValue = materialData != null && materialData.containsKey("materialType") ? 
                    (String) materialData.get("materialType") : materialType;
            Boolean isFreeValue = materialData != null && materialData.containsKey("isFree") ? 
                    (Boolean) materialData.get("isFree") : isFree;

            if (materialTitle == null || materialTitle.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
            }

            StudyMaterial material = new StudyMaterial();
            material.setTitle(materialTitle);
            material.setMaterialType(materialTypeValue != null ? materialTypeValue : "PDF");
            material.setFileUrl(fileUrl);
            material.setFileName(fileName);
            material.setFileSize(fileSize);
            material.setIsFree(isFreeValue != null ? isFreeValue : false);
            
            if (materialData != null && materialData.containsKey("description")) {
                material.setDescription((String) materialData.get("description"));
            }

            StudyMaterial saved = courseService.addMaterialToLecture(lectureId, material);
            
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "fileUrl", fileUrl,
                    "message", "File uploaded successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @AuthenticationPrincipal User principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "general") String type) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            // Check file size based on file type (videos and audio can be larger)
            String contentType = file.getContentType();
            long maxSize = 50 * 1024 * 1024; // 50MB default
            String maxSizeLabel = "50MB";
            
            if (contentType != null && contentType.startsWith("video/")) {
                maxSize = 1024L * 1024L * 1024L; // 1GB for videos
                maxSizeLabel = "1GB";
            } else if (contentType != null && contentType.startsWith("audio/")) {
                maxSize = 200L * 1024L * 1024L; // 200MB for audio
                maxSizeLabel = "200MB";
            }
            
            if (file.getSize() > maxSize) {
                return ResponseEntity.status(413).body(Map.of(
                    "error", "File size exceeds maximum limit of " + maxSizeLabel,
                    "fileSize", file.getSize(),
                    "maxSize", maxSize
                ));
            }
            
            String subDirectory = switch (type.toLowerCase()) {
                case "assignment" -> "assignments";
                case "material" -> "materials";
                default -> "general";
            };
            
            String fileUrl = fileUploadService.uploadFile(file, subDirectory);
            long fileSize = fileUploadService.getFileSize(fileUrl);
            
            return ResponseEntity.ok(Map.of(
                    "fileUrl", fileUrl,
                    "fileName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "file",
                    "fileSize", fileSize,
                    "message", "File uploaded successfully"
            ));
        } catch (org.springframework.web.multipart.MaxUploadSizeExceededException e) {
            log.warn("Max upload size exceeded for file '{}' (size={}): {}", file.getOriginalFilename(), file.getSize(), e.getMessage());
            return ResponseEntity.status(413).body(Map.of(
                "error", "File size exceeds maximum limit (1GB for videos, 200MB for audio, 50MB for others)",
                "message", "Please choose a smaller file"
            ));
        } catch (Exception e) {
            log.error("Error uploading file '{}' of type '{}': {}", file != null ? file.getOriginalFilename() : "null", type, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage() != null ? e.getMessage() : "Upload failed",
                "message", "An error occurred while uploading the file"
            ));
        }
    }
}

