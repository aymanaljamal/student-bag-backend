package com.studentbag.backend.notifications.dto.request;

import com.studentbag.backend.domain.enums.notifications.NotificationAttachmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationAttachmentRequest {

    @NotNull
    private NotificationAttachmentType type;

    @NotBlank
    private String url;

    private String fileName;
}