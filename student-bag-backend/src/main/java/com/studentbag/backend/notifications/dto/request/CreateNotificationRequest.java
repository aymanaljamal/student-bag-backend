package com.studentbag.backend.notifications.dto.request;

import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CreateNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    @NotNull
    private NotificationType type;

    @NotNull
    private NotificationPriority priority;

    @NotNull
    private NotificationChannel channel;

    @NotNull
    private NotificationTargetType targetType;

    private String targetValue;
    private String imageUrl;
    private String iconUrl;

    private boolean broadcastToAll = false;

    private LocalDateTime scheduledAt;
    private LocalDateTime expiresAt;

    private List<UUID> recipientUserIds = new ArrayList<>();

    @Valid
    private List<NotificationAttachmentRequest> attachments = new ArrayList<>();
}