package com.lms.service;

import com.lms.domain.Notification;
import com.lms.domain.UserAccount;
import com.lms.repository.NotificationRepository;
import com.lms.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public Notification createNotification(Long userId, String title, String message, Notification.NotificationType type, String actionUrl) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setActionUrl(actionUrl);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("User can only mark their own notifications as read");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("User can only delete their own notifications");
        }

        notificationRepository.delete(notification);
    }
}

