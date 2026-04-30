package com.studentbag.backend.analytics.dto.reports;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportsOverviewResponse {

    private List<ReportMetricCard> metricCards;

    private AcademicStructureReport academicStructure;
    private UsersReport users;
    private EventsReport events;
    private ResourcesReport resources;
    private ProductivityReport productivity;
    private ScheduleReport schedules;
    private GradeReport grades;
    private NotificationReport notifications;

    private List<ChartPoint> usersByRoleChart;
    private List<ChartPoint> resourcesByStatusChart;
    private List<ChartPoint> eventsByTypeChart;
    private List<ChartPoint> notificationsByStatusChart;
    private List<ChartPoint> academicStructureChart;
    private List<TimeSeriesPoint> weeklyActivityChart;

    private List<ReportHighlight> highlights;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportMetricCard {
        private String key;
        private String title;
        private String subtitle;
        private Long value;
        private String icon;
        private String colorToken;
        private String trendLabel;
        private Double trendPercent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicStructureReport {
        private Long institutions;
        private Long activeInstitutions;
        private Long faculties;
        private Long activeFaculties;
        private Long departments;
        private Long activeDepartments;
        private Long courses;
        private Long activeCourses;
        private Long courseSections;
        private Long classSessions;
        private Long terms;
        private Long currentTerms;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsersReport {
        private Long totalUsers;
        private Long students;
        private Long instructors;
        private Long administrators;
        private Long activeStudents;
        private Long usersCreatedThisWeek;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventsReport {
        private Long totalEvents;
        private Long upcomingEvents;
        private Long finishedEvents;
        private Long opportunities;
        private Long registrations;
        private Long eventsCreatedThisWeek;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourcesReport {
        private Long totalResources;
        private Long approvedResources;
        private Long pendingResources;
        private Long rejectedResources;
        private Long visibleResources;
        private Long personalFolders;
        private Long personalItems;
        private Long resourcesCreatedThisWeek;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductivityReport {
        private Long totalNotes;
        private Long importantNotes;
        private Long pinnedNotes;
        private Long noteAttachments;
        private Long totalTasks;
        private Long activeTasks;
        private Long completedTasks;
        private Long overdueTasks;
        private Long tasksDueToday;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleReport {
        private Long totalSchedules;
        private Long activeSchedules;
        private Long archivedSchedules;
        private Long scheduleEntries;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeReport {
        private Long calculations;
        private Long calculationItems;
        private Long createdThisWeek;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationReport {
        private Long campaigns;
        private Long campaignsToday;
        private Long sentTotal;
        private Long sentToday;
        private Long sentThisMonth;
        private Long unread;
        private Long read;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPoint {
        private String label;
        private Long value;
        private String colorToken;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        private LocalDate date;
        private Long value;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportHighlight {
        private String key;
        private String title;
        private String description;
        private String icon;
        private String colorToken;
        private Long value;
    }
}