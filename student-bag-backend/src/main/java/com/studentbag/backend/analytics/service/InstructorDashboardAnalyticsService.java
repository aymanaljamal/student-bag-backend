package com.studentbag.backend.analytics.service;

import com.studentbag.backend.analytics.dto.dashboard.InstructorDashboardAnalyticsResponse;

public interface InstructorDashboardAnalyticsService {

    InstructorDashboardAnalyticsResponse getMyDashboardAnalyticsByEmail(String email);
}