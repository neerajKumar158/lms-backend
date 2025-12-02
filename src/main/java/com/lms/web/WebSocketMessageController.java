package com.lms.web;

import com.lms.repository.UserAccountRepository;
import com.lms.service.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket message controller for real-time messaging.
 * Handles STOMP messages sent via WebSocket.
 */
@Slf4j
@Controller
public class WebSocketMessageController {

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages via WebSocket
     * Client sends to: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> payload, 
                           @AuthenticationPrincipal User user) {
        try {
            Long senderId = userAccountRepository.findByEmail(user.getUsername())
                    .orElseThrow().getId();
            Long conversationId = Long.parseLong(payload.get("conversationId").toString());
            String content = payload.get("content") != null ? payload.get("content").toString() : "";
            String attachmentUrl = payload.get("attachmentUrl") != null ? payload.get("attachmentUrl").toString() : null;
            String attachmentName = payload.get("attachmentName") != null ? payload.get("attachmentName").toString() : null;

            messagingService.sendMessage(conversationId, senderId, content, attachmentUrl, attachmentName);
            log.info("WebSocket message sent from user {} to conversation {}", senderId, conversationId);
        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle typing indicators
     * Client sends to: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> payload,
                            @AuthenticationPrincipal User user) {
        try {
            Long conversationId = Long.parseLong(payload.get("conversationId").toString());
            Boolean isTyping = payload.get("isTyping") != null ? 
                    Boolean.parseBoolean(payload.get("isTyping").toString()) : false;
            
            // Broadcast typing status to conversation participants
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/typing", Map.of(
                "userId", userAccountRepository.findByEmail(user.getUsername()).orElseThrow().getId(),
                "isTyping", isTyping
            ));
        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage(), e);
        }
    }
}

