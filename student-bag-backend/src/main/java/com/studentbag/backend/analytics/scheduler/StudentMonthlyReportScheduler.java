package com.studentbag.backend.analytics.scheduler;

import com.studentbag.backend.analytics.service.StudentMonthlyReportNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentMonthlyReportScheduler {

    private final StudentMonthlyReportNotificationService monthlyReportNotificationService;

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Jerusalem")
    public void sendWeeklyReports() {
        log.info("Starting weekly student report notification dispatch");

        monthlyReportNotificationService.dispatchMonthlyReports();

        log.info("Finished weekly student report notification dispatch");
    }
}