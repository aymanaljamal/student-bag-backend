package com.studentbag.backend.analytics.service.impl;

import com.studentbag.backend.analytics.dto.dashboard.ChartDataPointDto;
import com.studentbag.backend.analytics.dto.dashboard.StudentDashboardAnalyticsResponse;
import com.studentbag.backend.analytics.dto.dashboard.TimeSeriesPointDto;
import com.studentbag.backend.analytics.mapper.DashboardAnalyticsMapper;
import com.studentbag.backend.analytics.repository.AnalyticsQueryRepository;
import com.studentbag.backend.analytics.service.StudentDashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentDashboardAnalyticsServiceImpl implements StudentDashboardAnalyticsService {

    private final AnalyticsQueryRepository queryRepository;
    private final DashboardAnalyticsMapper mapper;

    @Override
    public StudentDashboardAnalyticsResponse getMyDashboardAnalyticsByEmail(String email) {
        Long studentId = queryRepository.resolveStudentIdByEmail(email);

        return StudentDashboardAnalyticsResponse.builder()
                .taskAnalytics(buildTaskAnalytics(studentId))
                .noteAnalytics(buildNoteAnalytics(studentId))
                .scheduleAnalytics(buildScheduleAnalytics(studentId))
                .gradeAnalytics(buildGradeAnalytics(studentId))
                .libraryAnalytics(buildLibraryAnalytics(studentId))
                .reminderAnalytics(buildReminderAnalytics(studentId))
                .eventAnalytics(buildEventAnalytics(studentId))
                .charts(buildCharts(studentId))
                .build();
    }

    private StudentDashboardAnalyticsResponse.TaskAnalytics buildTaskAnalytics(Long studentId) {
        var nearest = queryRepository.findNearestStudentTask(studentId);

        return mapper.toStudentTaskAnalytics(
                queryRepository.countStudentTasks(studentId),
                queryRepository.countStudentActiveTasks(studentId),
                queryRepository.countStudentCompletedTasks(studentId),
                queryRepository.countStudentOverdueTasks(studentId),
                queryRepository.countStudentTasksDueToday(studentId),
                queryRepository.countStudentTasksAddedThisWeek(studentId),
                nearest == null ? null : mapper.toNearestTask(
                        nearest.id(),
                        nearest.title(),
                        nearest.dueDateTime()
                )
        );
    }

    private StudentDashboardAnalyticsResponse.NoteAnalytics buildNoteAnalytics(Long studentId) {
        var lastEdited = queryRepository.findLastEditedStudentNote(studentId);

        return mapper.toStudentNoteAnalytics(
                queryRepository.countStudentNotes(studentId),
                queryRepository.countStudentPinnedNotes(studentId),
                queryRepository.countStudentImportantNotes(studentId),
                queryRepository.countStudentArchivedNotes(studentId),
                queryRepository.countStudentNotesAddedThisWeek(studentId),
                lastEdited == null ? null : mapper.toLastEditedNote(
                        lastEdited.id(),
                        lastEdited.title(),
                        lastEdited.updatedAt()
                )
        );
    }

    private StudentDashboardAnalyticsResponse.ScheduleAnalytics buildScheduleAnalytics(Long studentId) {
        return mapper.toStudentScheduleAnalytics(
                queryRepository.hasActiveSchedule(studentId),
                queryRepository.findActiveScheduleName(studentId),
                queryRepository.countActiveScheduleCourses(studentId),
                queryRepository.countActiveScheduleDaysOff(studentId),
                queryRepository.countTodayScheduleSessions(studentId)
        );
    }

    private StudentDashboardAnalyticsResponse.GradeAnalytics buildGradeAnalytics(Long studentId) {
        return mapper.toStudentGradeAnalytics(
                queryRepository.findStudentLatestGpa(studentId),
                queryRepository.findStudentLatestPercentage(studentId),
                queryRepository.countStudentGradeCalculations(studentId)
        );
    }

    private StudentDashboardAnalyticsResponse.LibraryAnalytics buildLibraryAnalytics(Long studentId) {
        return mapper.toStudentLibraryAnalytics(
                queryRepository.countStudentLibraryFolders(studentId),
                queryRepository.countStudentLibraryItems(studentId),
                queryRepository.countStudentCopiedFromPublic(studentId)
        );
    }

    private StudentDashboardAnalyticsResponse.ReminderAnalytics buildReminderAnalytics(Long studentId) {
        return mapper.toStudentReminderAnalytics(
                queryRepository.countStudentUpcomingReminders(studentId)
        );
    }

    private StudentDashboardAnalyticsResponse.EventAnalytics buildEventAnalytics(Long studentId) {
        var nearest = queryRepository.findNearestStudentEvent(studentId);

        return mapper.toStudentEventAnalytics(
                queryRepository.countStudentRegisteredEvents(studentId),
                nearest == null ? null : mapper.toNearestEvent(
                        nearest.id(),
                        nearest.title(),
                        nearest.startDateTime()
                )
        );
    }
    private StudentDashboardAnalyticsResponse.StudentCharts buildCharts(Long studentId) {
        List<ChartDataPointDto> tasksByStatus = queryRepository.studentTasksByStatus(studentId)
                .stream()
                .map(row -> mapper.chartPoint(row.label(), row.value()))
                .toList();

        List<ChartDataPointDto> notesByStatus = queryRepository.studentNotesByStatus(studentId)
                .stream()
                .map(row -> mapper.chartPoint(row.label(), row.value()))
                .toList();

        List<TimeSeriesPointDto> weeklyTasksCreated = queryRepository.studentTasksCreatedThisWeek(studentId)
                .stream()
                .map(row -> mapper.timePoint(row.date(), row.value()))
                .toList();

        List<TimeSeriesPointDto> weeklyNotesCreated = queryRepository.studentNotesCreatedThisWeek(studentId)
                .stream()
                .map(row -> mapper.timePoint(row.date(), row.value()))
                .toList();

        List<TimeSeriesPointDto> gradeTrend = queryRepository.studentGradeTrend(studentId)
                .stream()
                .map(row -> mapper.timePoint(row.date(), row.value()))
                .toList();

        List<TimeSeriesPointDto> upcomingEventsTimeline = queryRepository.studentUpcomingEventsTimeline(studentId)
                .stream()
                .map(row -> mapper.timePoint(row.date(), row.value()))
                .toList();

        return mapper.toStudentCharts(
                tasksByStatus,
                notesByStatus,
                weeklyTasksCreated,
                weeklyNotesCreated,
                gradeTrend,
                upcomingEventsTimeline
        );
    }
}