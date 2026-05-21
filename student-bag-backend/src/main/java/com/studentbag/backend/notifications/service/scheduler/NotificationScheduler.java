package com.studentbag.backend.notifications.service.scheduler;

import com.studentbag.backend.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    // التذكيرات المتكررة: كل يوم الساعة 8 صباحاً
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Jerusalem")
    public void dispatchRecurringTaskNotifications() {
        log.info("Starting recurring task notifications dispatch");

        notificationService.dispatchRecurringTaskNotifications();

        log.info("Finished recurring task notifications dispatch");
    }

    // تذكير أحداث بكرا: كل يوم الساعة 8:10 صباحاً
    @Scheduled(cron = "0 10 8 * * *", zone = "Asia/Jerusalem")
    public void dispatchEventNotifications() {
        log.info("Starting event notifications dispatch");

        notificationService.dispatchEventNotifications();

        log.info("Finished event notifications dispatch");
    }
}