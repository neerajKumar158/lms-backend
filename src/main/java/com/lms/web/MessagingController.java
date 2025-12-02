package com.lms.web;

import com.lms.domain.Conversation;
import com.lms.domain.Course;
import com.lms.domain.Message;
import com.lms.domain.UserAccount;
import com.lms.repository.UserAccountRepository;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for messaging functionality.
 * Handles HTTP endpoints for conversations and messages.
 */
@Slf4j
@RestController
@RequestMapping("/api/lms/messaging")
public class MessagingController {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Get list of users that the current user can start a conversation with.
     * For now this returns all users in the same organization (if any),
     * otherwise all users in the system.
     * The current user is included so that the dropdown is never empty
     * even in single-user test environments.
     */
    @GetMapping("/users")
    public ResponseEntity<?> getMessagingUsers(@AuthenticationPrincipal User user) {
        try {
            UserAccount current = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<UserAccount> candidates;
            if (current.getOrganization() != null) {
                candidates = userAccountRepository.findByOrganizationId(current.getOrganization().getId());
            } else {
                candidates = userAccountRepository.findAll();
            }

            List<Map<String, Object>> result = candidates.stream()
                    .map(u -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", u.getId());
                        userMap.put("name", u.getName() != null ? u.getName() : "");
                        userMap.put("email", u.getEmail() != null ? u.getEmail() : "");
                        userMap.put("userType", u.getUserType() != null ? u.getUserType().name() : null);
                        return userMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error loading messaging users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get list of courses that the current user can associate with a conversation.
     * Returns courses the user is enrolled in (if student) or teaching (if teacher).
     */
    @GetMapping("/courses")
    public ResponseEntity<?> getMessagingCourses(@AuthenticationPrincipal User user) {
        try {
            UserAccount current = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Course> courses;
            
            // If student, get enrolled courses
            if (current.getUserType() == UserAccount.UserType.STUDENT) {
                courses = enrollmentService.getStudentEnrollments(current.getId()).stream()
                        .map(enrollment -> enrollment.getCourse())
                        .filter(course -> course != null)
                        .collect(Collectors.toList());
            } 
            // If teacher, get courses they're teaching
            else if (current.getUserType() == UserAccount.UserType.TEACHER) {
                courses = courseService.getCoursesByInstructor(current.getId());
            } 
            // For other types (ADMIN, ORGANIZATION), return all published courses
            else {
                courses = courseService.getAllPublishedCourses();
            }

            List<Map<String, Object>> result = courses.stream()
                    .map(c -> {
                        Map<String, Object> courseMap = new HashMap<>();
                        courseMap.put("id", c.getId());
                        courseMap.put("title", c.getTitle() != null ? c.getTitle() : "");
                        courseMap.put("description", c.getDescription() != null ? c.getDescription() : "");
                        return courseMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error loading messaging courses: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get or create a conversation with another user
     */
    @PostMapping("/conversation")
    public ResponseEntity<?> getOrCreateConversation(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId1 = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("Current user not found")).getId();
            
            Object userIdObj = request.get("userId");
            if (userIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
            }
            Long userId2 = Long.parseLong(userIdObj.toString());
            
            if (userId1.equals(userId2)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot start conversation with yourself"));
            }
            
            Long courseId = null;
            Object courseIdObj = request.get("courseId");
            if (courseIdObj != null && !courseIdObj.toString().trim().isEmpty()) {
                try {
                    courseId = Long.parseLong(courseIdObj.toString());
                } catch (NumberFormatException e) {
                    log.warn("Invalid courseId format: {}", courseIdObj);
                    // Continue without courseId - it's optional
                }
            }

            log.info("Calling messagingService.getOrCreateConversation with userId1={}, userId2={}, courseId={}", userId1, userId2, courseId);
            Conversation conversation = messagingService.getOrCreateConversation(userId1, userId2, courseId);
            log.info("Conversation created/retrieved successfully: {}", conversation.getId());
            
            // Build a safe response object to avoid serialization issues
            Map<String, Object> response = new HashMap<>();
            response.put("id", conversation.getId());
            response.put("createdAt", conversation.getCreatedAt());
            response.put("lastMessageAt", conversation.getLastMessageAt());
            
            // Add participant info
            Map<String, Object> p1 = new HashMap<>();
            if (conversation.getParticipant1() != null) {
                p1.put("id", conversation.getParticipant1().getId());
                p1.put("name", conversation.getParticipant1().getName());
                p1.put("email", conversation.getParticipant1().getEmail());
                p1.put("userType", conversation.getParticipant1().getUserType() != null ? conversation.getParticipant1().getUserType().name() : null);
            }
            response.put("participant1", p1);
            
            Map<String, Object> p2 = new HashMap<>();
            if (conversation.getParticipant2() != null) {
                p2.put("id", conversation.getParticipant2().getId());
                p2.put("name", conversation.getParticipant2().getName());
                p2.put("email", conversation.getParticipant2().getEmail());
                p2.put("userType", conversation.getParticipant2().getUserType() != null ? conversation.getParticipant2().getUserType().name() : null);
            }
            response.put("participant2", p2);
            
            // Add course info if present (handle lazy loading safely)
            // Use the courseId we already have instead of accessing lazy-loaded course
            if (courseId != null) {
                try {
                    Optional<Course> courseOpt = courseService.getCourseById(courseId);
                    if (courseOpt.isPresent()) {
                        Course course = courseOpt.get();
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("id", course.getId());
                        courseInfo.put("title", course.getTitle() != null ? course.getTitle() : "");
                        response.put("course", courseInfo);
                    }
                } catch (Exception e) {
                    log.warn("Error loading course {} for response: {}", courseId, e.getMessage());
                    // Course is optional, continue without it
                }
            }
            
            log.info("Returning conversation response with ID: {}", response.get("id"));
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.error("Invalid number format in request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid user ID or course ID format", "details", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Runtime error getting/creating conversation: {}", e.getMessage(), e);
            String errorMsg = e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " - " + e.getCause().getMessage();
            }
            return ResponseEntity.badRequest().body(Map.of("error", errorMsg, "type", e.getClass().getSimpleName()));
        } catch (Exception e) {
            log.error("Unexpected error getting/creating conversation: {}", e.getMessage(), e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Failed to create conversation";
            if (e.getCause() != null) {
                errorMsg += " - " + e.getCause().getMessage();
            }
            return ResponseEntity.status(500).body(Map.of(
                "error", errorMsg,
                "type", e.getClass().getSimpleName(),
                "message", "An unexpected error occurred. Please check server logs for details."
            ));
        }
    }

    /**
     * Get all conversations for the current user
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(@AuthenticationPrincipal User user) {
        try {
            Long userId = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();
            List<Conversation> conversations = messagingService.getUserConversations(userId);
            
            // Build safe response objects to avoid serialization issues
            List<Map<String, Object>> response = conversations.stream().map(conv -> {
                Map<String, Object> convMap = new HashMap<>();
                convMap.put("id", conv.getId());
                convMap.put("createdAt", conv.getCreatedAt());
                convMap.put("lastMessageAt", conv.getLastMessageAt());
                
                // Add participant info
                Map<String, Object> p1 = new HashMap<>();
                p1.put("id", conv.getParticipant1().getId());
                p1.put("name", conv.getParticipant1().getName());
                p1.put("email", conv.getParticipant1().getEmail());
                p1.put("userType", conv.getParticipant1().getUserType() != null ? conv.getParticipant1().getUserType().name() : null);
                convMap.put("participant1", p1);
                
                Map<String, Object> p2 = new HashMap<>();
                p2.put("id", conv.getParticipant2().getId());
                p2.put("name", conv.getParticipant2().getName());
                p2.put("email", conv.getParticipant2().getEmail());
                p2.put("userType", conv.getParticipant2().getUserType() != null ? conv.getParticipant2().getUserType().name() : null);
                convMap.put("participant2", p2);
                
                // Add course info if present
                if (conv.getCourse() != null) {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("id", conv.getCourse().getId());
                    courseInfo.put("title", conv.getCourse().getTitle());
                    convMap.put("course", courseInfo);
                }
                
                return convMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting conversations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get messages in a conversation
     */
    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId) {
        try {
            List<Message> messages = messagingService.getConversationMessages(conversationId);
            
            // Build safe response objects to avoid serialization issues
            List<Map<String, Object>> response = messages.stream().map(msg -> {
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("id", msg.getId());
                msgMap.put("content", msg.getContent());
                msgMap.put("sentAt", msg.getSentAt());
                msgMap.put("isRead", msg.getIsRead());
                msgMap.put("readAt", msg.getReadAt());
                msgMap.put("attachmentUrl", msg.getAttachmentUrl());
                msgMap.put("attachmentName", msg.getAttachmentName());
                
                // Add sender info
                if (msg.getSender() != null) {
                    Map<String, Object> senderMap = new HashMap<>();
                    senderMap.put("id", msg.getSender().getId());
                    senderMap.put("name", msg.getSender().getName());
                    senderMap.put("email", msg.getSender().getEmail());
                    senderMap.put("userType", msg.getSender().getUserType() != null ? msg.getSender().getUserType().name() : null);
                    msgMap.put("sender", senderMap);
                }
                
                return msgMap;
            }).collect(Collectors.toList());
            
            log.debug("Returning {} messages for conversation {}", response.size(), conversationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting messages: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Send a message (HTTP endpoint - WebSocket is also available)
     */
    @PostMapping("/conversation/{conversationId}/message")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> request) {
        try {
            Long senderId = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();
            String content = request.get("content") != null ? request.get("content").toString() : "";
            String attachmentUrl = request.get("attachmentUrl") != null && !request.get("attachmentUrl").toString().isEmpty() ? 
                    request.get("attachmentUrl").toString() : null;
            String attachmentName = request.get("attachmentName") != null && !request.get("attachmentName").toString().isEmpty() ? 
                    request.get("attachmentName").toString() : null;
            
            if (content == null || content.trim().isEmpty()) {
                if (attachmentUrl == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Message content or attachment is required"));
                }
            }
            
            log.info("Sending message to conversation {} from user {}", conversationId, senderId);
            Message message = messagingService.sendMessage(conversationId, senderId, content, attachmentUrl, attachmentName);
            log.info("Message sent successfully with ID: {}", message.getId());
            
            // Build safe response object
            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("content", message.getContent());
            response.put("sentAt", message.getSentAt());
            response.put("isRead", message.getIsRead());
            response.put("readAt", message.getReadAt());
            response.put("attachmentUrl", message.getAttachmentUrl());
            response.put("attachmentName", message.getAttachmentName());
            
            // Add sender info
            if (message.getSender() != null) {
                Map<String, Object> senderMap = new HashMap<>();
                senderMap.put("id", message.getSender().getId());
                senderMap.put("name", message.getSender().getName());
                senderMap.put("email", message.getSender().getEmail());
                senderMap.put("userType", message.getSender().getUserType() != null ? message.getSender().getUserType().name() : null);
                response.put("sender", senderMap);
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error sending message: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * Upload file attachment for message
     */
    @PostMapping("/attachment/upload")
    public ResponseEntity<?> uploadAttachment(
            @AuthenticationPrincipal User user,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            // Use existing file upload service
            String uploadDir = "uploads/messages";
            java.io.File uploadDirectory = new java.io.File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + "/" + fileName;
            java.io.File dest = new java.io.File(filePath);
            file.transferTo(dest);
            
            String fileUrl = "/uploads/messages/" + fileName;
            return ResponseEntity.ok(Map.of(
                "url", fileUrl,
                "name", file.getOriginalFilename(),
                "size", file.getSize()
            ));
        } catch (Exception e) {
            log.error("Error uploading attachment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId) {
        try {
            Long userId = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow().getId();
            messagingService.markMessagesAsRead(conversationId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User user) {
        try {
            Long userId = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow().getId();
            Long count = messagingService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error getting unread count: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search messages in a conversation
     */
    @GetMapping("/conversation/{conversationId}/search")
    public ResponseEntity<?> searchMessages(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId,
            @RequestParam String keyword) {
        try {
            List<Message> messages = messagingService.searchMessages(conversationId, keyword);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error searching messages: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

