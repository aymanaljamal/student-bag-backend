package com.studentbag.backend.analytics.controller;

import com.studentbag.backend.analytics.dto.dashboard.InstructorDashboardAnalyticsResponse;
import com.studentbag.backend.analytics.service.InstructorDashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/instructor")
@RequiredArgsConstructor
public class InstructorDashboardAnalyticsController {

    private final InstructorDashboardAnalyticsService service;

    @GetMapping("/dashboard")
    public InstructorDashboardAnalyticsResponse getMyDashboard(Authentication authentication) {
        return service.getMyDashboardAnalyticsByEmail(authentication.getName());
    }
}