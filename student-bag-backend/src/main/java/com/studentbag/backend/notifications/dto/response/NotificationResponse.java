package com.studentbag.backend.notifications.dto.response;

import com.studentbag.backend.domain.enums.notifications.UserNotificationStatus;
import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID userNotificationId;
    private UUID notificationId;

    private String title;
    private String body;

    private NotificationType type;
    private NotificationPriority priority;
    private NotificationChannel channel;
    private NotificationTargetType targetType;

    private String targetValue;
    private String imageUrl;
    private String iconUrl;

    private boolean read;
    private UserNotificationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;

    private List<NotificationAttachmentResponse> attachments;
}