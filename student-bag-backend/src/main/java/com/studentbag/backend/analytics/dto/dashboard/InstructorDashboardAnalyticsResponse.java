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
public class InstructorDashboardAnalyticsResponse {

    private ResourceAnalytics resourceAnalytics;
    private EventAnalytics eventAnalytics;
    private TeachingAnalytics teachingAnalytics;
    private InstructorCharts charts;

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
        private Long totalCreated;
        private Long active;
        private Long ended;
        private Long upcoming;
        private Long totalRegistrations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeachingAnalytics {
        private Long sectionCount;
        private Long totalCapacity;
        private Long totalEnrolled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorCharts {
        private List<ChartDataPointDto> resourcesByStatus;
        private List<TimeSeriesPointDto> resourcesCreatedWeekly;
        private List<ChartDataPointDto> eventsByStatus;
        private List<TimeSeriesPointDto> eventRegistrationsWeekly;
        private List<ChartDataPointDto> capacityVsEnrolled;
    }
}