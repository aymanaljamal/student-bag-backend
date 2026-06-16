package com.studentbag.backend.resources.scheduler;

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
public class WeeklyResourceNotificationScheduler {

    private final WeeklyResourceNotificationService weeklyResourceNotificationService;

    @Value("${student-bag.notifications.zone:Asia/Hebron}")
    private String zoneId;

    @Value("${student-bag.notifications.resources.send-first-on-startup:false}")
    private boolean sendFirstOnStartup;

    @Value("${student-bag.notifications.resources.first-run-date:}")
    private String firstRunDate;

    @Scheduled(
            cron = "${student-bag.notifications.resources.weekly-cron:0 0 18 ? * TUE}",
            zone = "${student-bag.notifications.zone:Asia/Hebron}"
    )
    public void dispatchWeeklyResourceNotifications() {
        log.info("Weekly resource notification scheduler started.");

        weeklyResourceNotificationService.dispatchWeeklyResourceNotifications();

        log.info("Weekly resource notification scheduler finished.");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void dispatchFirstNotificationOnStartup() {
        if (!sendFirstOnStartup) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of(zoneId));

        if (firstRunDate == null || firstRunDate.isBlank()) {
            return;
        }

        if (!today.toString().equals(firstRunDate.trim())) {
            return;
        }

        log.info("First weekly resource notification startup dispatch started for date={}", today);

        weeklyResourceNotificationService.dispatchWeeklyResourceNotifications();

        log.info("First weekly resource notification startup dispatch finished.");
    }
}