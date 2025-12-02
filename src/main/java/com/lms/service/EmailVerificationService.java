package com.lms.service;

import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Email Verification Service - Phase 2.2
 * Handles email verification for user registration
 */
@Slf4j
@Service
public class EmailVerificationService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:9192}")
    private String baseUrl;

    @Value("${app.email-verification-enabled:true}")
    private boolean emailVerificationEnabled;

    /**
     * Generate and send verification email
     */
    @Transactional
    public void sendVerificationEmail(UserAccount user) {
        if (!emailVerificationEnabled || mailSender == null) {
            // In development, auto-verify if email is disabled
            // Reload entity to avoid merge issues
            UserAccount savedUser = userAccountRepository.findById(user.getId())
                    .orElse(user);
            savedUser.setEmailVerified(true);
            savedUser.setUpdatedAt(LocalDateTime.now());
            userAccountRepository.save(savedUser);
            return;
        }

        String token = UUID.randomUUID().toString();
        // Reload entity to avoid merge issues with collections
        UserAccount savedUser = userAccountRepository.findById(user.getId())
                .orElse(user);
        savedUser.setEmailVerificationToken(token);
        savedUser.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours
        savedUser.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(savedUser);

        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify Your Email - LMS Portal");
        message.setText("Please click the following link to verify your email address:\n\n" + verificationUrl +
                "\n\nThis link will expire in 24 hours.\n\nIf you didn't create an account, please ignore this email.");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage(), e);
            // In development, continue without email
        }
    }

    /**
     * Verify email using token
     */
    @Transactional
    public boolean verifyEmail(String token) {
        UserAccount user = userAccountRepository.findByEmailVerificationToken(token)
                .orElse(null);

        if (user == null) {
            return false;
        }

        // Check if token is expired
        if (user.getEmailVerificationTokenExpiry() != null &&
            user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(user);

        return true;
    }

    /**
     * Resend verification email
     */
    @Transactional
    public boolean resendVerificationEmail(String email) {
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElse(null);

        if (user == null || user.getEmailVerified()) {
            return false;
        }

        sendVerificationEmail(user);
        return true;
    }
}

