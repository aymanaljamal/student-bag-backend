package com.studentbag.backend.analytics.controller;

import com.studentbag.backend.analytics.dto.dashboard.StudentDashboardAnalyticsResponse;
import com.studentbag.backend.analytics.service.StudentDashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/student")
@RequiredArgsConstructor
public class StudentDashboardAnalyticsController {

    private final StudentDashboardAnalyticsService service;

    @GetMapping("/dashboard")
    public StudentDashboardAnalyticsResponse getMyDashboard(Authentication authentication) {
        return service.getMyDashboardAnalyticsByEmail(authentication.getName());
    }
}