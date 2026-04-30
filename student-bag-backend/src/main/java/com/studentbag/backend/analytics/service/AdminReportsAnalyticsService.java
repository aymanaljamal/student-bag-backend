package com.studentbag.backend.analytics.service;

import com.studentbag.backend.analytics.dto.reports.AdminReportsOverviewResponse;

public interface AdminReportsAnalyticsService {

    AdminReportsOverviewResponse getOverview(String adminEmail);
}