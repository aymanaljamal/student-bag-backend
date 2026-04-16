package com.studentbag.backend.notifications.dto.request;

import com.studentbag.backend.domain.enums.notifications.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class CreateAdminNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    @NotNull
    private NotificationType type;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Builder.Default
    private NotificationChannel channel = NotificationChannel.BOTH;

    @Builder.Default
    private NotificationTargetType targetType = NotificationTargetType.NONE;

    private String targetValue;
    private String imageUrl;
    private String iconUrl;

    @NotNull
    private NotificationAudienceType audienceType;

    private List<UUID> userIds = new ArrayList<>();
    private List<Long> studentIds = new ArrayList<>();
}