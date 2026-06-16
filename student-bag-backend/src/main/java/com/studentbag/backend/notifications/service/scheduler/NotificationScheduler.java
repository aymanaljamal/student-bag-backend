package com.studentbag.backend.notifications.service.scheduler;

import com.studentbag.backend.notifications.service.NotificationService;
import com.studentbag.backend.resources.service.WeeklyResourceNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final WeeklyResourceNotificationService weeklyResourceNotificationService;

    @Value("${student-bag.notifications.zone:Asia/Hebron}")
    private String zoneId;

    @Value("${student-bag.notifications.resources.send-first-on-startup:false}")
    private boolean sendFirstResourceNotificationOnStartup;

    @Value("${student-bag.notifications.resources.first-run-date:}")
    private String firstResourceNotificationRunDate;

    // التذكيرات المتكررة: كل يوم الساعة 8 صباحاً
    @Scheduled(
            cron = "${student-bag.notifications.recurring-tasks.daily-cron:0 0 8 * * *}",
            zone = "${student-bag.notifications.zone:Asia/Hebron}"
    )
    public void dispatchRecurringTaskNotifications() {
        try {
            log.info("Starting recurring task notifications dispatch");

            notificationService.dispatchRecurringTaskNotifications();

            log.info("Finished recurring task notifications dispatch");
        } catch (Exception e) {
            log.error("Failed to dispatch recurring task notifications", e);
        }
    }

    // تذكير أحداث بكرا: كل يوم الساعة 8:10 صباحاً
    @Scheduled(
            cron = "${student-bag.notifications.events.daily-cron:0 10 8 * * *}",
            zone = "${student-bag.notifications.zone:Asia/Hebron}"
    )
    public void dispatchEventNotifications() {
        try {
            log.info("Starting event notifications dispatch");

            notificationService.dispatchEventNotifications();

            log.info("Finished event notifications dispatch");
        } catch (Exception e) {
            log.error("Failed to dispatch event notifications", e);
        }
    }

    // إشعار الموارد الأسبوعي: كل ثلاثاء الساعة 6 مساءً
    @Scheduled(
            cron = "${student-bag.notifications.resources.weekly-cron:0 0 18 ? * TUE}",
            zone = "${student-bag.notifications.zone:Asia/Hebron}"
    )
    public void dispatchWeeklyResourceNotifications() {
        try {
            log.info("Starting weekly resource notifications dispatch");

            weeklyResourceNotificationService.dispatchWeeklyResourceNotifications();

            log.info("Finished weekly resource notifications dispatch");
        } catch (Exception e) {
            log.error("Failed to dispatch weekly resource notifications", e);
        }
    }

    // أول إشعار موارد عند تشغيل السيرفر حسب التاريخ الموجود في application.yml
    @EventListener(ApplicationReadyEvent.class)
    public void dispatchFirstWeeklyResourceNotificationOnStartup() {
        if (!sendFirstResourceNotificationOnStartup) {
            return;
        }

        if (firstResourceNotificationRunDate == null
                || firstResourceNotificationRunDate.isBlank()) {
            log.warn("First weekly resource notification skipped because first-run-date is empty.");
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of(zoneId));

        if (!today.toString().equals(firstResourceNotificationRunDate.trim())) {
            log.info(
                    "First weekly resource notification skipped. today={}, configuredDate={}",
                    today,
                    firstResourceNotificationRunDate
            );
            return;
        }

        try {
            log.info("Starting first weekly resource notification dispatch on startup");

            weeklyResourceNotificationService.dispatchWeeklyResourceNotifications();

            log.info("Finished first weekly resource notification dispatch on startup");
        } catch (Exception e) {
            log.error("Failed to dispatch first weekly resource notification on startup", e);
        }
    }
}