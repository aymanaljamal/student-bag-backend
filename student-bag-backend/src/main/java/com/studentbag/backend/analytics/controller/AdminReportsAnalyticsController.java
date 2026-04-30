package com.studentbag.backend.analytics.controller;

import com.studentbag.backend.analytics.dto.reports.AdminReportsOverviewResponse;
import com.studentbag.backend.analytics.service.AdminReportsAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/admin/reports")
@RequiredArgsConstructor
public class AdminReportsAnalyticsController {

    private final AdminReportsAnalyticsService service;

    @GetMapping("/overview")
    public AdminReportsOverviewResponse getOverview(Authentication authentication) {
        return service.getOverview(authentication.getName());
    }
}