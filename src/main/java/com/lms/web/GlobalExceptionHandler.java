package com.lms.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        System.err.println("Global exception handler caught error: " + e.getMessage());
        e.printStackTrace();
        
        // Check if this is an API request
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/")) {
            // Return JSON error for API requests
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } else {
            // Return error page for UI requests
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            modelAndView.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return modelAndView;
        }
    }
}

