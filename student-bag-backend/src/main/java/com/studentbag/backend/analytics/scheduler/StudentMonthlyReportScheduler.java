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

    // يشتغل أول يوم من كل شهر الساعة 9 صباحاً
    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Jerusalem")
    public void sendMonthlyReports() {
        log.info("Starting monthly student report notification dispatch");

        monthlyReportNotificationService.dispatchMonthlyReports();

        log.info("Finished monthly student report notification dispatch");
    }
}