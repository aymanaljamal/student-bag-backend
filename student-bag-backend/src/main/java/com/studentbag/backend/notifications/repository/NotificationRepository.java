package com.studentbag.backend.notifications.repository;

import com.studentbag.backend.domain.enums.notifications.NotificationType;
import com.studentbag.backend.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(notification) > 0 THEN true ELSE false END
            FROM Notification notification
            JOIN notification.recipients recipient
            WHERE recipient.user.id = :userId
              AND notification.type = :type
              AND notification.targetValue = :targetValue
              AND notification.createdAt >= :from
              AND notification.createdAt < :to
            """)
    boolean existsForRecipientAndTypeAndTargetValueInWindow(
            @Param("userId") UUID userId,
            @Param("type") NotificationType type,
            @Param("targetValue") String targetValue,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}