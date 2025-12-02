package com.lms.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/**
 * Handles multipart file upload exceptions. This exception handler manages
 * file size limit violations and provides user-friendly error responses for
 * upload failures in the LMS platform.
 *
 * @author VisionWaves
 * @version 1.0
 */
@RestControllerAdvice
public class MultipartExceptionHandler {

    /**
     * Handles file size limit exceeded exceptions and returns appropriate error response.
     *
     * @param e the MaxUploadSizeExceededException
     * @return the error response entity
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "error", "File size exceeds maximum limit",
                "message", "The uploaded file is too large. Maximum file size is 50MB.",
                "maxSize", "50MB"
        ));
    }
}



