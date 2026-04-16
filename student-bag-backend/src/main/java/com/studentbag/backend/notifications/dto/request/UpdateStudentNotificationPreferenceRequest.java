package com.studentbag.backend.notifications.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStudentNotificationPreferenceRequest {

    @NotNull
    private Boolean eventNotificationsEnabled;

    @NotNull
    private Boolean taskNotificationsEnabled;

    @NotNull
    private Boolean recurringTaskNotificationsEnabled;

    @NotNull
    private Boolean taskReminderOneDayBeforeEnabled;

    @NotNull
    private Boolean monthlyStatsNotificationsEnabled;
}