package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.dto.context.*;
import com.studentbag.backend.chatbot.service.AiPromptBuilderService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

@Service
public class AiPromptBuilderServiceImpl implements AiPromptBuilderService {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String buildSystemPrompt() {

        return """
                You are University Bot, an academic assistant inside Student Bag app.

                Your role:
                - Help university students manage academic life.
                - Answer clearly and practically.
                - Use only provided student data.
                - Never invent fake schedule/tasks/grades.
                - Be concise and student-friendly.
                - Support Arabic and English.
                - Prioritize deadlines and urgent tasks.
                - If data is missing, say so clearly.
                - Focus on academic productivity.

                Available modules:
                - Tasks
                - Notes
                - Schedule
                - Events
                - Grades
                - Resources
                - Dashboard analytics

                Never expose technical JSON.
                Never mention internal backend structures.
                """;
    }

    @Override
    public String buildStudentPrompt(
            StudentAiContextDto context,
            String userQuestion
    ) {

        StringBuilder sb = new StringBuilder();

        appendStudentProfile(sb, context);

        appendDashboard(sb, context);

        appendTodaySchedule(sb, context);

        appendUpcomingSchedule(sb, context);

        appendTasks(sb, context);

        appendNotes(sb, context);

        appendResources(sb, context);

        appendEvents(sb, context);

        appendGrades(sb, context);

        sb.append("\n");
        sb.append("=== USER QUESTION ===\n");
        sb.append(userQuestion);

        return sb.toString();
    }

    private void appendStudentProfile(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        if (context.getStudent() == null) {
            return;
        }

        sb.append("=== STUDENT ===\n");

        sb.append("Name: ")
                .append(nullSafe(context.getStudent().getFullName()))
                .append("\n");

        sb.append("Major: ")
                .append(nullSafe(context.getStudent().getUniversityMajor()))
                .append("\n");

        sb.append("Academic Level: ")
                .append(nullSafe(context.getStudent().getAcademicLevel()))
                .append("\n\n");
    }

    private void appendDashboard(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        DashboardAiContext dashboard = context.getDashboard();

        if (dashboard == null) {
            return;
        }

        sb.append("=== DASHBOARD ===\n");

        sb.append("Active Tasks: ")
                .append(dashboard.getActiveTasks())
                .append("\n");

        sb.append("Completed Tasks: ")
                .append(dashboard.getCompletedTasks())
                .append("\n");

        sb.append("Overdue Tasks: ")
                .append(dashboard.getOverdueTasks())
                .append("\n");

        sb.append("Due Today Tasks: ")
                .append(dashboard.getDueTodayTasks())
                .append("\n");

        sb.append("Total Notes: ")
                .append(dashboard.getTotalNotes())
                .append("\n");

        sb.append("Important Notes: ")
                .append(dashboard.getImportantNotes())
                .append("\n");

        sb.append("Pinned Notes: ")
                .append(dashboard.getPinnedNotes())
                .append("\n");

        sb.append("Has Active Schedule: ")
                .append(dashboard.getHasActiveSchedule())
                .append("\n");

        sb.append("Active Schedule Name: ")
                .append(dashboard.getActiveScheduleName())
                .append("\n");

        sb.append("Today Sessions: ")
                .append(dashboard.getTodaySessions())
                .append("\n");

        sb.append("Latest GPA: ")
                .append(dashboard.getLatestGpa())
                .append("\n");

        sb.append("Latest Percentage: ")
                .append(dashboard.getLatestPercentage())
                .append("\n");

        sb.append("Upcoming Reminders: ")
                .append(dashboard.getUpcomingReminders())
                .append("\n");

        sb.append("Registered Events: ")
                .append(dashboard.getRegisteredEvents())
                .append("\n\n");
    }
    private void appendTodaySchedule(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        if (context.getTodaySchedule() == null ||
                context.getTodaySchedule().isEmpty()) {
            return;
        }

        sb.append("=== TODAY SCHEDULE ===\n");

        for (ScheduleEntryAiContext entry : context.getTodaySchedule()) {

            sb.append("- ");

            sb.append(entry.getTitle());

            sb.append(" | ");

            sb.append(formatDate(entry.getStartDateTime()));

            sb.append(" -> ");

            sb.append(formatDate(entry.getEndDateTime()));

            if (entry.getLocation() != null) {
                sb.append(" | ");
                sb.append(entry.getLocation());
            }

            sb.append("\n");
        }

        sb.append("\n");
    }

    private void appendUpcomingSchedule(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        if (context.getUpcomingSchedule() == null ||
                context.getUpcomingSchedule().isEmpty()) {
            return;
        }

        sb.append("=== UPCOMING SCHEDULE ===\n");

        for (ScheduleEntryAiContext entry : context.getUpcomingSchedule()) {

            sb.append("- ")
                    .append(entry.getTitle())
                    .append(" | ")
                    .append(formatDate(entry.getStartDateTime()))
                    .append("\n");
        }

        sb.append("\n");
    }

    private void appendTasks(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        appendTaskGroup(
                sb,
                "ACTIVE TASKS",
                context.getActiveTasks()
        );

        appendTaskGroup(
                sb,
                "OVERDUE TASKS",
                context.getOverdueTasks()
        );

        appendTaskGroup(
                sb,
                "DUE TODAY TASKS",
                context.getDueTodayTasks()
        );
    }

    private void appendTaskGroup(
            StringBuilder sb,
            String title,
            java.util.List<TaskAiContext> tasks
    ) {

        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        sb.append("=== ").append(title).append(" ===\n");

        for (TaskAiContext task : tasks) {

            sb.append("- ");

            sb.append(task.getTitle());

            if (task.getPriority() != null) {
                sb.append(" | Priority: ");
                sb.append(task.getPriority());
            }

            if (task.getDueDateTime() != null) {
                sb.append(" | Due: ");
                sb.append(formatDate(task.getDueDateTime()));
            }

            sb.append("\n");
        }

        sb.append("\n");
    }

    private void appendNotes(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        if (context.getImportantNotes() == null ||
                context.getImportantNotes().isEmpty()) {
            return;
        }

        sb.append("=== NOTES ===\n");

        for (NoteAiContext note : context.getImportantNotes()) {

            sb.append("- ")
                    .append(note.getTitle());

            if (note.getCourseName() != null) {
                sb.append(" | ");
                sb.append(note.getCourseName());
            }

            sb.append("\n");
        }

        sb.append("\n");
    }

    private void appendResources(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        if (context.getResources() == null ||
                context.getResources().isEmpty()) {
            return;
        }

        sb.append("=== RESOURCES ===\n");

        for (ResourceAiContext resource : context.getResources()) {

            sb.append("- ")
                    .append(resource.getTitle());

            if (resource.getCategory() != null) {
                sb.append(" | ");
                sb.append(resource.getCategory());
            }

            sb.append("\n");
        }

        sb.append("\n");
    }

    private void appendEvents(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        if (context.getUpcomingEvents() == null ||
                context.getUpcomingEvents().isEmpty()) {
            return;
        }

        sb.append("=== EVENTS ===\n");

        for (EventAiContext event : context.getUpcomingEvents()) {

            sb.append("- ")
                    .append(event.getTitle());

            if (event.getStartDateTime() != null) {
                sb.append(" | ");
                sb.append(formatDate(event.getStartDateTime()));
            }

            sb.append("\n");
        }

        sb.append("\n");
    }

    private void appendGrades(
            StringBuilder sb,
            StudentAiContextDto context
    ) {

        GradeAiContext grades = context.getGrades();

        if (grades == null) {
            return;
        }

        sb.append("=== GRADES ===\n");

        sb.append("GPA: ")
                .append(grades.getCalculatedGpa())
                .append("\n");

        sb.append("Percentage: ")
                .append(grades.getCalculatedPercentage())
                .append("\n");

        if (grades.getCourses() != null &&
                !grades.getCourses().isEmpty()) {

            sb.append("Courses:\n");

            for (GradeCourseAiContext course : grades.getCourses()) {

                sb.append("- ");

                sb.append(course.getCourseName());

                if (course.getNormalizedPercentage() != null) {
                    sb.append(" | ");
                    sb.append(course.getNormalizedPercentage());
                    sb.append("%");
                }

                sb.append("\n");
            }
        }

        sb.append("\n");
    }

    private String formatDate(java.time.LocalDateTime dateTime) {

        if (dateTime == null) {
            return "-";
        }

        return dateTime.format(DATE_TIME);
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}