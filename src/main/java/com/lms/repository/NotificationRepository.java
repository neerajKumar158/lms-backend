package com.lms.repository;

import com.lms.domain.Notification;
import com.lms.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(UserAccount user);
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(UserAccount user);
    Long countByUserAndIsReadFalse(UserAccount user);
}



