package com.lms.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        return handleException(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        return handleException(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        return handleException(e, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Object handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        return handleException(e, request, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("timestamp", LocalDateTime.now().toString());
            
            Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                            (existing, replacement) -> existing
                    ));
            error.put("fieldErrors", fieldErrors);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", "Invalid input. Please check your form data.");
            modelAndView.addObject("status", HttpStatus.BAD_REQUEST.value());
            modelAndView.setStatus(HttpStatus.BAD_REQUEST);
            return modelAndView;
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("timestamp", LocalDateTime.now().toString());
            
            Map<String, String> violations = e.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (existing, replacement) -> existing
                    ));
            error.put("violations", violations);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else {
            return handleException(e, request, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        return handleException(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Object handleException(Exception e, HttpServletRequest request, HttpStatus status) {
        String path = request.getRequestURI();

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Unhandled exception at {}: {} - {}", path, e.getClass().getSimpleName(), e.getMessage(), e);
        } else {
            log.warn("Handled exception at {}: {} - {}", path, e.getClass().getSimpleName(), e.getMessage());
        }
        
        // Check if this is an API request
        if (path != null && path.startsWith("/api/")) {
            // Return JSON error for API requests
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            error.put("status", status.value());
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("path", path);
            
            // Don't expose internal details in production
            if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
                error.put("message", "An internal server error occurred. Please try again later.");
            }
            
            return ResponseEntity.status(status).body(error);
        } else {
            // Return error page for UI requests
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            modelAndView.addObject("status", status.value());
            modelAndView.setStatus(status);
            return modelAndView;
        }
    }
}

