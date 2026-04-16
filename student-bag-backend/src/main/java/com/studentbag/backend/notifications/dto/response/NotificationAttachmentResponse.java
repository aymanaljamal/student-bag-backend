package com.studentbag.backend.notifications.dto.response;

import com.studentbag.backend.domain.enums.notifications.NotificationAttachmentType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationAttachmentResponse {
    private UUID id;
    private NotificationAttachmentType type;
    private String url;
    private String fileName;
}