package com.studentbag.backend.notifications.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InstitutionAdminMessageStatsResponse {

    private long totalMessages;
    private long openMessages;
    private long resolvedMessages;
    private long archivedMessages;

    private long totalAdminRecipients;
    private long readByAdmins;
    private long unreadByAdmins;
}