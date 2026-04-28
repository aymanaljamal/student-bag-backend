package com.studentbag.backend.analytics.controller;

import com.studentbag.backend.analytics.dto.dashboard.AdminDashboardAnalyticsResponse;
import com.studentbag.backend.analytics.service.AdminDashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/admin")
@RequiredArgsConstructor
public class AdminDashboardAnalyticsController {

    private final AdminDashboardAnalyticsService service;

    @GetMapping("/dashboard")
    public AdminDashboardAnalyticsResponse getDashboard(Authentication authentication) {
        return service.getDashboardAnalyticsByEmail(authentication.getName());
    }
}