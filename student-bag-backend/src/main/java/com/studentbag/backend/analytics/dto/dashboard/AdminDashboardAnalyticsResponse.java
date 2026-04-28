package com.studentbag.backend.analytics.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardAnalyticsResponse {

    private UserAnalytics userAnalytics;
    private ResourceAnalytics resourceAnalytics;
    private EventAnalytics eventAnalytics;
    private NotificationAnalytics notificationAnalytics;
    private AcademicAnalytics academicAnalytics;
    private AdminCharts charts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnalytics {
        private Long total;
        private Long students;
        private Long instructors;
        private Long admins;
        private Long newThisMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceAnalytics {
        private Long total;
        private Long pending;
        private Long approved;
        private Long rejected;
        private Long removed;
        private Double approvalRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventAnalytics {
        private Long total;
        private Long active;
        private Long ended;
        private Long upcoming;
        private Long requiresRegistrationCount;
        private Long totalRegistrations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationAnalytics {
        private Long sentToday;
        private Long sentThisMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicAnalytics {
        private Long institutionCount;
        private Long courseCount;
        private Long facultyCount;
        private Long departmentCount;
        private Long termCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminCharts {
        private List<ChartDataPointDto> usersByRole;
        private List<TimeSeriesPointDto> newUsersMonthly;
        private List<ChartDataPointDto> resourcesByStatus;
        private List<ChartDataPointDto> eventsByStatus;
        private List<TimeSeriesPointDto> notificationsMonthly;
        private List<ChartDataPointDto> academicCounts;
    }
}