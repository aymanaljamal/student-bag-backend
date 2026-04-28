package com.studentbag.backend.analytics.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardAnalyticsResponse {

    private TaskAnalytics taskAnalytics;
    private NoteAnalytics noteAnalytics;
    private ScheduleAnalytics scheduleAnalytics;
    private GradeAnalytics gradeAnalytics;
    private LibraryAnalytics libraryAnalytics;
    private ReminderAnalytics reminderAnalytics;
    private EventAnalytics eventAnalytics;
    private StudentCharts charts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskAnalytics {
        private Long total;
        private Long active;
        private Long completed;
        private Long overdue;
        private Long dueToday;
        private Double completionRate;
        private Long addedThisWeek;
        private NearestTask nearestTask;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearestTask {
        private Long id;
        private String title;
        private LocalDateTime dueDateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteAnalytics {
        private Long total;
        private Long pinned;
        private Long important;
        private Long archived;
        private Long addedThisWeek;
        private LastEditedNote lastEditedNote;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastEditedNote {
        private Long id;
        private String title;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleAnalytics {
        private Boolean hasActiveSchedule;
        private String activeScheduleName;
        private Long courseCount;
        private Long daysOffCount;
        private Long todaySessions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeAnalytics {
        private Double latestGpa;
        private Double latestPercentage;
        private Long calculationCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LibraryAnalytics {
        private Long folderCount;
        private Long itemCount;
        private Long copiedFromPublicCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderAnalytics {
        private Long upcomingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventAnalytics {
        private Long registeredCount;
        private NearestEvent nearestEvent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearestEvent {
        private Long id;
        private String title;
        private LocalDateTime startDateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentCharts {
        private List<ChartDataPointDto> tasksByStatus;
        private List<ChartDataPointDto> notesByStatus;
        private List<TimeSeriesPointDto> weeklyTasksCreated;
        private List<TimeSeriesPointDto> weeklyNotesCreated;
        private List<TimeSeriesPointDto> gradeTrend;
        private List<TimeSeriesPointDto> upcomingEventsTimeline;
    }
}