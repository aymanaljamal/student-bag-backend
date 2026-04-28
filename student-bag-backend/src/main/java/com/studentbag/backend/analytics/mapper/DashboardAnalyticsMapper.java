package com.studentbag.backend.analytics.mapper;

import com.studentbag.backend.analytics.dto.dashboard.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DashboardAnalyticsMapper {

    public StudentDashboardAnalyticsResponse.TaskAnalytics toStudentTaskAnalytics(
            Long total,
            Long active,
            Long completed,
            Long overdue,
            Long dueToday,
            Long addedThisWeek,
            StudentDashboardAnalyticsResponse.NearestTask nearestTask
    ) {
        return StudentDashboardAnalyticsResponse.TaskAnalytics.builder()
                .total(n(total))
                .active(n(active))
                .completed(n(completed))
                .overdue(n(overdue))
                .dueToday(n(dueToday))
                .addedThisWeek(n(addedThisWeek))
                .completionRate(rate(completed, total))
                .nearestTask(nearestTask)
                .build();
    }

    public StudentDashboardAnalyticsResponse.NearestTask toNearestTask(
            Long id,
            String title,
            LocalDateTime dueDateTime
    ) {
        if (id == null) return null;

        return StudentDashboardAnalyticsResponse.NearestTask.builder()
                .id(id)
                .title(title)
                .dueDateTime(dueDateTime)
                .build();
    }

    public StudentDashboardAnalyticsResponse.NoteAnalytics toStudentNoteAnalytics(
            Long total,
            Long pinned,
            Long important,
            Long archived,
            Long addedThisWeek,
            StudentDashboardAnalyticsResponse.LastEditedNote lastEditedNote
    ) {
        return StudentDashboardAnalyticsResponse.NoteAnalytics.builder()
                .total(n(total))
                .pinned(n(pinned))
                .important(n(important))
                .archived(n(archived))
                .addedThisWeek(n(addedThisWeek))
                .lastEditedNote(lastEditedNote)
                .build();
    }

    public StudentDashboardAnalyticsResponse.LastEditedNote toLastEditedNote(
            Long id,
            String title,
            LocalDateTime updatedAt
    ) {
        if (id == null) return null;

        return StudentDashboardAnalyticsResponse.LastEditedNote.builder()
                .id(id)
                .title(title)
                .updatedAt(updatedAt)
                .build();
    }

    public StudentDashboardAnalyticsResponse.ScheduleAnalytics toStudentScheduleAnalytics(
            Boolean hasActiveSchedule,
            String activeScheduleName,
            Long courseCount,
            Long daysOffCount,
            Long todaySessions
    ) {
        return StudentDashboardAnalyticsResponse.ScheduleAnalytics.builder()
                .hasActiveSchedule(Boolean.TRUE.equals(hasActiveSchedule))
                .activeScheduleName(activeScheduleName)
                .courseCount(n(courseCount))
                .daysOffCount(n(daysOffCount))
                .todaySessions(n(todaySessions))
                .build();
    }

    public StudentDashboardAnalyticsResponse.GradeAnalytics toStudentGradeAnalytics(
            Double latestGpa,
            Double latestPercentage,
            Long calculationCount
    ) {
        return StudentDashboardAnalyticsResponse.GradeAnalytics.builder()
                .latestGpa(latestGpa)
                .latestPercentage(latestPercentage)
                .calculationCount(n(calculationCount))
                .build();
    }

    public StudentDashboardAnalyticsResponse.LibraryAnalytics toStudentLibraryAnalytics(
            Long folderCount,
            Long itemCount,
            Long copiedFromPublicCount
    ) {
        return StudentDashboardAnalyticsResponse.LibraryAnalytics.builder()
                .folderCount(n(folderCount))
                .itemCount(n(itemCount))
                .copiedFromPublicCount(n(copiedFromPublicCount))
                .build();
    }

    public StudentDashboardAnalyticsResponse.ReminderAnalytics toStudentReminderAnalytics(
            Long upcomingCount
    ) {
        return StudentDashboardAnalyticsResponse.ReminderAnalytics.builder()
                .upcomingCount(n(upcomingCount))
                .build();
    }

    public StudentDashboardAnalyticsResponse.EventAnalytics toStudentEventAnalytics(
            Long registeredCount,
            StudentDashboardAnalyticsResponse.NearestEvent nearestEvent
    ) {
        return StudentDashboardAnalyticsResponse.EventAnalytics.builder()
                .registeredCount(n(registeredCount))
                .nearestEvent(nearestEvent)
                .build();
    }

    public StudentDashboardAnalyticsResponse.NearestEvent toNearestEvent(
            Long id,
            String title,
            LocalDateTime startDateTime
    ) {
        if (id == null) return null;

        return StudentDashboardAnalyticsResponse.NearestEvent.builder()
                .id(id)
                .title(title)
                .startDateTime(startDateTime)
                .build();
    }

    public StudentDashboardAnalyticsResponse.StudentCharts toStudentCharts(
            List<ChartDataPointDto> tasksByStatus,
            List<ChartDataPointDto> notesByStatus,
            List<TimeSeriesPointDto> weeklyTasksCreated,
            List<TimeSeriesPointDto> weeklyNotesCreated,
            List<TimeSeriesPointDto> gradeTrend,
            List<TimeSeriesPointDto> upcomingEventsTimeline
    ) {
        return StudentDashboardAnalyticsResponse.StudentCharts.builder()
                .tasksByStatus(safeList(tasksByStatus))
                .notesByStatus(safeList(notesByStatus))
                .weeklyTasksCreated(safeList(weeklyTasksCreated))
                .weeklyNotesCreated(safeList(weeklyNotesCreated))
                .gradeTrend(safeList(gradeTrend))
                .upcomingEventsTimeline(safeList(upcomingEventsTimeline))
                .build();
    }

    public InstructorDashboardAnalyticsResponse.ResourceAnalytics toInstructorResourceAnalytics(
            Long total,
            Long pending,
            Long approved,
            Long rejected,
            Long removed
    ) {
        return InstructorDashboardAnalyticsResponse.ResourceAnalytics.builder()
                .total(n(total))
                .pending(n(pending))
                .approved(n(approved))
                .rejected(n(rejected))
                .removed(n(removed))
                .approvalRate(rate(approved, total))
                .build();
    }

    public InstructorDashboardAnalyticsResponse.EventAnalytics toInstructorEventAnalytics(
            Long totalCreated,
            Long active,
            Long ended,
            Long upcoming,
            Long totalRegistrations
    ) {
        return InstructorDashboardAnalyticsResponse.EventAnalytics.builder()
                .totalCreated(n(totalCreated))
                .active(n(active))
                .ended(n(ended))
                .upcoming(n(upcoming))
                .totalRegistrations(n(totalRegistrations))
                .build();
    }

    public InstructorDashboardAnalyticsResponse.TeachingAnalytics toTeachingAnalytics(
            Long sectionCount,
            Long totalCapacity,
            Long totalEnrolled
    ) {
        return InstructorDashboardAnalyticsResponse.TeachingAnalytics.builder()
                .sectionCount(n(sectionCount))
                .totalCapacity(n(totalCapacity))
                .totalEnrolled(n(totalEnrolled))
                .build();
    }

    public InstructorDashboardAnalyticsResponse.InstructorCharts toInstructorCharts(
            List<ChartDataPointDto> resourcesByStatus,
            List<TimeSeriesPointDto> resourcesCreatedWeekly,
            List<ChartDataPointDto> eventsByStatus,
            List<TimeSeriesPointDto> eventRegistrationsWeekly,
            List<ChartDataPointDto> capacityVsEnrolled
    ) {
        return InstructorDashboardAnalyticsResponse.InstructorCharts.builder()
                .resourcesByStatus(safeList(resourcesByStatus))
                .resourcesCreatedWeekly(safeList(resourcesCreatedWeekly))
                .eventsByStatus(safeList(eventsByStatus))
                .eventRegistrationsWeekly(safeList(eventRegistrationsWeekly))
                .capacityVsEnrolled(safeList(capacityVsEnrolled))
                .build();
    }

    public AdminDashboardAnalyticsResponse.UserAnalytics toAdminUserAnalytics(
            Long total,
            Long students,
            Long instructors,
            Long admins,
            Long newThisMonth
    ) {
        return AdminDashboardAnalyticsResponse.UserAnalytics.builder()
                .total(n(total))
                .students(n(students))
                .instructors(n(instructors))
                .admins(n(admins))
                .newThisMonth(n(newThisMonth))
                .build();
    }

    public AdminDashboardAnalyticsResponse.ResourceAnalytics toAdminResourceAnalytics(
            Long total,
            Long pending,
            Long approved,
            Long rejected,
            Long removed
    ) {
        return AdminDashboardAnalyticsResponse.ResourceAnalytics.builder()
                .total(n(total))
                .pending(n(pending))
                .approved(n(approved))
                .rejected(n(rejected))
                .removed(n(removed))
                .approvalRate(rate(approved, total))
                .build();
    }

    public AdminDashboardAnalyticsResponse.EventAnalytics toAdminEventAnalytics(
            Long total,
            Long active,
            Long ended,
            Long upcoming,
            Long requiresRegistrationCount,
            Long totalRegistrations
    ) {
        return AdminDashboardAnalyticsResponse.EventAnalytics.builder()
                .total(n(total))
                .active(n(active))
                .ended(n(ended))
                .upcoming(n(upcoming))
                .requiresRegistrationCount(n(requiresRegistrationCount))
                .totalRegistrations(n(totalRegistrations))
                .build();
    }

    public AdminDashboardAnalyticsResponse.NotificationAnalytics toAdminNotificationAnalytics(
            Long sentToday,
            Long sentThisMonth
    ) {
        return AdminDashboardAnalyticsResponse.NotificationAnalytics.builder()
                .sentToday(n(sentToday))
                .sentThisMonth(n(sentThisMonth))
                .build();
    }

    public AdminDashboardAnalyticsResponse.AcademicAnalytics toAdminAcademicAnalytics(
            Long institutionCount,
            Long courseCount,
            Long facultyCount,
            Long departmentCount,
            Long termCount
    ) {
        return AdminDashboardAnalyticsResponse.AcademicAnalytics.builder()
                .institutionCount(n(institutionCount))
                .courseCount(n(courseCount))
                .facultyCount(n(facultyCount))
                .departmentCount(n(departmentCount))
                .termCount(n(termCount))
                .build();
    }

    public AdminDashboardAnalyticsResponse.AdminCharts toAdminCharts(
            List<ChartDataPointDto> usersByRole,
            List<TimeSeriesPointDto> newUsersMonthly,
            List<ChartDataPointDto> resourcesByStatus,
            List<ChartDataPointDto> eventsByStatus,
            List<TimeSeriesPointDto> notificationsMonthly,
            List<ChartDataPointDto> academicCounts
    ) {
        return AdminDashboardAnalyticsResponse.AdminCharts.builder()
                .usersByRole(safeList(usersByRole))
                .newUsersMonthly(safeList(newUsersMonthly))
                .resourcesByStatus(safeList(resourcesByStatus))
                .eventsByStatus(safeList(eventsByStatus))
                .notificationsMonthly(safeList(notificationsMonthly))
                .academicCounts(safeList(academicCounts))
                .build();
    }

    public ChartDataPointDto chartPoint(String label, Long value) {
        return ChartDataPointDto.builder()
                .label(label)
                .value(n(value))
                .build();
    }

    public TimeSeriesPointDto timePoint(LocalDate date, Long value) {
        return TimeSeriesPointDto.builder()
                .date(date)
                .value(n(value))
                .build();
    }

    private Long n(Long value) {
        return value == null ? 0L : value;
    }

    private Double rate(Long part, Long total) {
        long safeTotal = n(total);
        if (safeTotal == 0) return 0.0;

        return Math.round((n(part) * 10000.0 / safeTotal)) / 100.0;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}