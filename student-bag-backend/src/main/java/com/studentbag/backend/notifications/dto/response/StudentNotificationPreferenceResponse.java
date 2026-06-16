package com.studentbag.backend.notifications.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StudentNotificationPreferenceResponse {

    private Boolean eventNotificationsEnabled;

    private Boolean taskNotificationsEnabled;

    private Boolean recurringTaskNotificationsEnabled;

    private Boolean taskReminderOneDayBeforeEnabled;

    private Boolean monthlyStatsNotificationsEnabled;

    private Boolean weeklyResourceNotificationsEnabled;
}