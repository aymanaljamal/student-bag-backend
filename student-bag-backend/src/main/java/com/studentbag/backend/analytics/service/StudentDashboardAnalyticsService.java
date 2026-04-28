package com.studentbag.backend.analytics.service;

import com.studentbag.backend.analytics.dto.dashboard.StudentDashboardAnalyticsResponse;

public interface StudentDashboardAnalyticsService {

    StudentDashboardAnalyticsResponse getMyDashboardAnalyticsByEmail(String email);
}