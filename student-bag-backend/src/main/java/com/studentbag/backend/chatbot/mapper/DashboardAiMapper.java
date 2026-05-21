package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.analytics.dto.dashboard.StudentDashboardAnalyticsResponse;
import com.studentbag.backend.chatbot.dto.context.DashboardAiContext;
import org.springframework.stereotype.Component;

@Component
public class DashboardAiMapper {

    public DashboardAiContext toContext(StudentDashboardAnalyticsResponse dto) {
        if (dto == null) return null;

        var task = dto.getTaskAnalytics();
        var note = dto.getNoteAnalytics();
        var schedule = dto.getScheduleAnalytics();
        var grade = dto.getGradeAnalytics();
        var reminder = dto.getReminderAnalytics();
        var event = dto.getEventAnalytics();

        return DashboardAiContext.builder()
                .activeTasks(task != null ? task.getActive() : null)
                .completedTasks(task != null ? task.getCompleted() : null)
                .overdueTasks(task != null ? task.getOverdue() : null)
                .dueTodayTasks(task != null ? task.getDueToday() : null)

                .totalNotes(note != null ? note.getTotal() : null)
                .importantNotes(note != null ? note.getImportant() : null)
                .pinnedNotes(note != null ? note.getPinned() : null)

                .hasActiveSchedule(schedule != null ? schedule.getHasActiveSchedule() : null)
                .activeScheduleName(schedule != null ? schedule.getActiveScheduleName() : null)
                .todaySessions(schedule != null ? schedule.getTodaySessions() : null)

                .latestGpa(grade != null ? grade.getLatestGpa() : null)
                .latestPercentage(grade != null ? grade.getLatestPercentage() : null)

                .upcomingReminders(reminder != null ? reminder.getUpcomingCount() : null)
                .registeredEvents(event != null ? event.getRegisteredCount() : null)
                .build();
    }
}