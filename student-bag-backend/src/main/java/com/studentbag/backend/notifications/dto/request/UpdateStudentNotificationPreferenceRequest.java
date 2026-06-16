package com.studentbag.backend.notifications.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentNotificationPreferenceRequest {

    private Boolean eventNotificationsEnabled;

    private Boolean taskNotificationsEnabled;

    private Boolean recurringTaskNotificationsEnabled;

    private Boolean taskReminderOneDayBeforeEnabled;

    private Boolean monthlyStatsNotificationsEnabled;

    private Boolean weeklyResourceNotificationsEnabled;
}