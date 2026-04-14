package com.studentbag.backend.notifications.mapper;

import com.studentbag.backend.notifications.dto.response.NotificationAttachmentResponse;
import com.studentbag.backend.notifications.dto.response.NotificationResponse;
import com.studentbag.backend.notifications.entity.NotificationAttachment;
import com.studentbag.backend.notifications.entity.UserNotification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(UserNotification userNotification) {
        var n = userNotification.getNotification();

        List<NotificationAttachmentResponse> attachments = n.getAttachments()
                .stream()
                .map(this::toAttachmentResponse)
                .toList();

        return NotificationResponse.builder()
                .userNotificationId(userNotification.getId())
                .notificationId(n.getId())
                .title(n.getTitle())
                .body(n.getBody())
                .type(n.getType())
                .priority(n.getPriority())
                .channel(n.getChannel())
                .targetType(n.getTargetType())
                .targetValue(n.getTargetValue())
                .imageUrl(n.getImageUrl())
                .iconUrl(n.getIconUrl())
                .read(userNotification.isReadFlag())
                .status(userNotification.getStatus())
                .createdAt(n.getCreatedAt())
                .deliveredAt(userNotification.getDeliveredAt())
                .readAt(userNotification.getReadAt())
                .expiresAt(n.getExpiresAt())
                .attachments(attachments)
                .build();
    }

    public NotificationAttachmentResponse toAttachmentResponse(NotificationAttachment attachment) {
        return NotificationAttachmentResponse.builder()
                .id(attachment.getId())
                .type(attachment.getType())
                .url(attachment.getUrl())
                .fileName(attachment.getFileName())
                .build();
    }
}