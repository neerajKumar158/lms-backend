package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Learning Management System (LMS) backend.
 * This class serves as the entry point for the Spring Boot application,
 * initializing the application context and starting the embedded server.
 *
 * @author VisionWaves
 * @version 1.0
 */
@SpringBootApplication
public class LmsBackendApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(LmsBackendApplication.class, args);
    }
}



