package com.studentbag.backend.notifications.dto.response;

import com.studentbag.backend.domain.enums.notifications.AdminMessageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InstitutionAdminMessageResponse {

    private Long id;

    private UUID senderUserId;
    private String senderName;
    private String senderEmail;

    private Long institutionId;
    private String institutionName;

    private String subject;
    private String body;

    private AdminMessageStatus status;

    private UUID notificationId;

    private long totalAdminRecipients;
    private long readByAdmins;
    private long unreadByAdmins;

    private Boolean readByCurrentUser;
    private LocalDateTime currentUserReadAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}