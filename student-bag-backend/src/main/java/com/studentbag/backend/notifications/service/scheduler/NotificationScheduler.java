package com.studentbag.backend.notifications.service.scheduler;

import com.studentbag.backend.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 60000)
    public void processNotifications() {
        notificationService.dispatchTaskReminderNotifications();
        notificationService.dispatchRecurringTaskNotifications();
        notificationService.dispatchEventNotifications();
    }
}