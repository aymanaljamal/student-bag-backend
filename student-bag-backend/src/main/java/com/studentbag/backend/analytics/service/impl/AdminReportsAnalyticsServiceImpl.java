package com.studentbag.backend.analytics.service.impl;

import com.studentbag.backend.analytics.dto.reports.AdminReportsOverviewResponse;
import com.studentbag.backend.analytics.repository.AnalyticsQueryRepository;
import com.studentbag.backend.analytics.service.AdminReportsAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportsAnalyticsServiceImpl implements AdminReportsAnalyticsService {

    private final AnalyticsQueryRepository repository;

    @Override
    public AdminReportsOverviewResponse getOverview(String adminEmail) {

        var academic = AdminReportsOverviewResponse.AcademicStructureReport.builder()
                .institutions(repository.countInstitutions())
                .activeInstitutions(repository.countActiveInstitutions())
                .faculties(repository.countFaculties())
                .activeFaculties(repository.countActiveFaculties())
                .departments(repository.countDepartments())
                .activeDepartments(repository.countActiveDepartments())
                .courses(repository.countCourses())
                .activeCourses(repository.countActiveCourses())
                .courseSections(repository.countCourseSections())
                .classSessions(repository.countClassSessions())
                .terms(repository.countTerms())
                .currentTerms(repository.countCurrentTerms())
                .build();

        var users = AdminReportsOverviewResponse.UsersReport.builder()
                .totalUsers(repository.countUsers())
                .students(repository.countStudents())
                .instructors(repository.countInstructors())
                .administrators(repository.countAdministrators())
                .activeStudents(repository.countActiveStudents())
                .usersCreatedThisWeek(repository.countUsersCreatedThisWeek())
                .build();

        var events = AdminReportsOverviewResponse.EventsReport.builder()
                .totalEvents(repository.countEvents())
                .upcomingEvents(repository.countUpcomingEvents())
                .finishedEvents(repository.countFinishedEvents())
                .opportunities(repository.countOpportunities())
                .registrations(repository.countEventRegistrations())
                .eventsCreatedThisWeek(repository.countEventsCreatedThisWeek())
                .build();

        var resources = AdminReportsOverviewResponse.ResourcesReport.builder()
                .totalResources(repository.countResources())
                .approvedResources(repository.countApprovedResources())
                .pendingResources(repository.countPendingResources())
                .rejectedResources(repository.countRejectedResources())
                .visibleResources(repository.countVisibleResources())
                .personalFolders(repository.countPersonalFolders())
                .personalItems(repository.countPersonalItems())
                .resourcesCreatedThisWeek(repository.countResourcesCreatedThisWeek())
                .build();

        var productivity = AdminReportsOverviewResponse.ProductivityReport.builder()
                .totalNotes(repository.countNotes())
                .importantNotes(repository.countImportantNotes())
                .pinnedNotes(repository.countPinnedNotes())
                .noteAttachments(repository.countNoteAttachments())
                .totalTasks(repository.countTasks())
                .activeTasks(repository.countActiveTasks())
                .completedTasks(repository.countCompletedTasks())
                .overdueTasks(repository.countOverdueTasks())
                .tasksDueToday(repository.countTasksDueToday())
                .build();

        var schedules = AdminReportsOverviewResponse.ScheduleReport.builder()
                .totalSchedules(repository.countSchedules())
                .activeSchedules(repository.countActiveSchedules())
                .archivedSchedules(repository.countArchivedSchedules())
                .scheduleEntries(repository.countScheduleEntries())
                .build();

        var grades = AdminReportsOverviewResponse.GradeReport.builder()
                .calculations(repository.countGradeCalculations())
                .calculationItems(repository.countGradeCalculationItems())
                .createdThisWeek(repository.countGradeCalculationsCreatedThisWeek())
                .build();

        var notifications = AdminReportsOverviewResponse.NotificationReport.builder()
                .campaigns(repository.countNotificationCampaigns())
                .campaignsToday(repository.countNotificationCampaignsToday())
                .sentTotal(repository.countNotificationsSentTotal())
                .sentToday(repository.countNotificationsSentToday())
                .sentThisMonth(repository.countNotificationsSentThisMonth())
                .unread(repository.countUnreadNotifications())
                .read(repository.countReadNotifications())
                .build();

        return AdminReportsOverviewResponse.builder()
                .metricCards(buildMetricCards(
                        academic,
                        users,
                        events,
                        resources,
                        productivity,
                        schedules,
                        grades,
                        notifications
                ))
                .academicStructure(academic)
                .users(users)
                .events(events)
                .resources(resources)
                .productivity(productivity)
                .schedules(schedules)
                .grades(grades)
                .notifications(notifications)
                .usersByRoleChart(toChart(repository.usersByRole(), "info"))
                .resourcesByStatusChart(toChart(repository.resourcesByStatus(), "warning"))
                .eventsByTypeChart(toChart(repository.eventsByType(), "primary"))
                .notificationsByStatusChart(toChart(repository.notificationsByStatus(), "error"))
                .academicStructureChart(buildAcademicChart(academic))
                .weeklyActivityChart(
                        repository.weeklyActivity()
                                .stream()
                                .map(row -> AdminReportsOverviewResponse.TimeSeriesPoint.builder()
                                        .date(row.date())
                                        .value(row.value())
                                        .build())
                                .toList()
                )
                .highlights(buildHighlights(resources, productivity, events, schedules, notifications))
                .build();
    }

    private List<AdminReportsOverviewResponse.ReportMetricCard> buildMetricCards(
            AdminReportsOverviewResponse.AcademicStructureReport academic,
            AdminReportsOverviewResponse.UsersReport users,
            AdminReportsOverviewResponse.EventsReport events,
            AdminReportsOverviewResponse.ResourcesReport resources,
            AdminReportsOverviewResponse.ProductivityReport productivity,
            AdminReportsOverviewResponse.ScheduleReport schedules,
            AdminReportsOverviewResponse.GradeReport grades,
            AdminReportsOverviewResponse.NotificationReport notifications
    ) {
        return List.of(
                card(
                        "users",
                        "admin.reports.cards.users.title",
                        "admin.reports.cards.users.subtitle",
                        users.getTotalUsers(),
                        "users",
                        "info",
                        users.getUsersCreatedThisWeek()
                ),
                card(
                        "academic",
                        "admin.reports.cards.academic.title",
                        "admin.reports.cards.academic.subtitle",
                        academic.getCourses(),
                        "school",
                        "primary",
                        academic.getCourseSections()
                ),
                card(
                        "resources",
                        "admin.reports.cards.resources.title",
                        "admin.reports.cards.resources.subtitle",
                        resources.getTotalResources(),
                        "folder",
                        "warning",
                        resources.getPendingResources()
                ),
                card(
                        "events",
                        "admin.reports.cards.events.title",
                        "admin.reports.cards.events.subtitle",
                        events.getUpcomingEvents(),
                        "calendar",
                        "success",
                        events.getEventsCreatedThisWeek()
                ),
                card(
                        "notifications",
                        "admin.reports.cards.notifications.title",
                        "admin.reports.cards.notifications.subtitle",
                        notifications.getSentTotal(),
                        "notifications",
                        "error",
                        notifications.getUnread()
                ),
                card(
                        "notes",
                        "admin.reports.cards.notes.title",
                        "admin.reports.cards.notes.subtitle",
                        productivity.getTotalNotes(),
                        "note",
                        "primaryLight",
                        productivity.getPinnedNotes()
                ),
                card(
                        "tasks",
                        "admin.reports.cards.tasks.title",
                        "admin.reports.cards.tasks.subtitle",
                        productivity.getActiveTasks(),
                        "task",
                        "error",
                        productivity.getOverdueTasks()
                ),
                card(
                        "schedules",
                        "admin.reports.cards.schedules.title",
                        "admin.reports.cards.schedules.subtitle",
                        schedules.getActiveSchedules(),
                        "schedule",
                        "info",
                        schedules.getScheduleEntries()
                ),
                card(
                        "grades",
                        "admin.reports.cards.grades.title",
                        "admin.reports.cards.grades.subtitle",
                        grades.getCalculations(),
                        "grade",
                        "success",
                        grades.getCreatedThisWeek()
                )
        );
    }

    private AdminReportsOverviewResponse.ReportMetricCard card(
            String key,
            String title,
            String subtitle,
            Long value,
            String icon,
            String colorToken,
            Long trendValue
    ) {
        return AdminReportsOverviewResponse.ReportMetricCard.builder()
                .key(key)
                .title(title)
                .subtitle(subtitle)
                .value(value == null ? 0L : value)
                .icon(icon)
                .colorToken(colorToken)
                .trendLabel(String.valueOf(trendValue == null ? 0L : trendValue))
                .trendPercent(null)
                .build();
    }

    private List<AdminReportsOverviewResponse.ChartPoint> buildAcademicChart(
            AdminReportsOverviewResponse.AcademicStructureReport academic
    ) {
        return List.of(
                chart("Institutions", academic.getInstitutions(), "info"),
                chart("Faculties", academic.getFaculties(), "primary"),
                chart("Departments", academic.getDepartments(), "warning"),
                chart("Courses", academic.getCourses(), "success"),
                chart("Sections", academic.getCourseSections(), "error")
        );
    }

    private List<AdminReportsOverviewResponse.ChartPoint> toChart(
            List<AnalyticsQueryRepository.ChartRow> rows,
            String colorToken
    ) {
        if (rows == null) {
            return List.of();
        }

        return rows.stream()
                .map(row -> chart(row.label(), row.value(), colorToken))
                .toList();
    }

    private AdminReportsOverviewResponse.ChartPoint chart(
            String label,
            Long value,
            String colorToken
    ) {
        return AdminReportsOverviewResponse.ChartPoint.builder()
                .label(label)
                .value(value == null ? 0L : value)
                .colorToken(colorToken)
                .build();
    }

    private List<AdminReportsOverviewResponse.ReportHighlight> buildHighlights(
            AdminReportsOverviewResponse.ResourcesReport resources,
            AdminReportsOverviewResponse.ProductivityReport productivity,
            AdminReportsOverviewResponse.EventsReport events,
            AdminReportsOverviewResponse.ScheduleReport schedules,
            AdminReportsOverviewResponse.NotificationReport notifications
    ) {
        return List.of(
                highlight(
                        "pending_resources",
                        "admin.reports.highlights.pendingResources.title",
                        "admin.reports.highlights.pendingResources.description",
                        "approval",
                        "warning",
                        resources.getPendingResources()
                ),
                highlight(
                        "notifications_today",
                        "admin.reports.highlights.notificationsToday.title",
                        "admin.reports.highlights.notificationsToday.description",
                        "notifications_active",
                        "error",
                        notifications.getSentToday()
                ),
                highlight(
                        "notes_sticker",
                        "admin.reports.highlights.notes.title",
                        "admin.reports.highlights.notes.description",
                        "sticky_note",
                        "primary",
                        productivity.getTotalNotes()
                ),
                highlight(
                        "tasks_today",
                        "admin.reports.highlights.tasksToday.title",
                        "admin.reports.highlights.tasksToday.description",
                        "today",
                        "error",
                        productivity.getTasksDueToday()
                ),
                highlight(
                        "upcoming_events",
                        "admin.reports.highlights.upcomingEvents.title",
                        "admin.reports.highlights.upcomingEvents.description",
                        "event",
                        "success",
                        events.getUpcomingEvents()
                ),
                highlight(
                        "active_schedules",
                        "admin.reports.highlights.activeSchedules.title",
                        "admin.reports.highlights.activeSchedules.description",
                        "timeline",
                        "info",
                        schedules.getActiveSchedules()
                )
        );
    }

    private AdminReportsOverviewResponse.ReportHighlight highlight(
            String key,
            String title,
            String description,
            String icon,
            String colorToken,
            Long value
    ) {
        return AdminReportsOverviewResponse.ReportHighlight.builder()
                .key(key)
                .title(title)
                .description(description)
                .icon(icon)
                .colorToken(colorToken)
                .value(value == null ? 0L : value)
                .build();
    }
}