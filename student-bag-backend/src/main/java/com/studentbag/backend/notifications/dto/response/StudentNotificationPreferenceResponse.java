package com.studentbag.backend.notifications.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentNotificationPreferenceResponse {
    private Boolean eventNotificationsEnabled;
    private Boolean taskNotificationsEnabled;
    private Boolean recurringTaskNotificationsEnabled;
    private Boolean taskReminderOneDayBeforeEnabled;
    private Boolean monthlyStatsNotificationsEnabled;
}