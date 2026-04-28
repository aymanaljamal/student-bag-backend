package com.studentbag.backend.analytics.service;

import com.studentbag.backend.analytics.dto.dashboard.AdminDashboardAnalyticsResponse;

public interface AdminDashboardAnalyticsService {

    AdminDashboardAnalyticsResponse getDashboardAnalyticsByEmail(String email);
}