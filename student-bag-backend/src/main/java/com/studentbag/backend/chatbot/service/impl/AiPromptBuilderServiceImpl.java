package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.dto.context.AiFileContentContext;
import com.studentbag.backend.chatbot.dto.context.AttachmentAiContext;
import com.studentbag.backend.chatbot.dto.context.DashboardAiContext;
import com.studentbag.backend.chatbot.dto.context.EventAiContext;
import com.studentbag.backend.chatbot.dto.context.GradeAiContext;
import com.studentbag.backend.chatbot.dto.context.GradeCourseAiContext;
import com.studentbag.backend.chatbot.dto.context.NoteAiContext;
import com.studentbag.backend.chatbot.dto.context.ResourceAiContext;
import com.studentbag.backend.chatbot.dto.context.ScheduleEntryAiContext;
import com.studentbag.backend.chatbot.dto.context.StudentAiContextDto;
import com.studentbag.backend.chatbot.dto.context.TaskAiContext;
import com.studentbag.backend.chatbot.service.AiPromptBuilderService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
                - Never invent fake schedule/tasks/grades/resources/files/events.
                - Be concise and student-friendly.
                - Support Arabic and English.
                - Prioritize deadlines, today schedule, overdue tasks, and urgent academic items.
                - If data is missing, say so clearly.
                - Focus on academic productivity.

                Available modules:
                - Tasks
                - Notes
                - Schedule
                - Events and opportunities
                - Grades
                - Resources
                - Dashboard analytics
                - Attached file content previews

                Schedule rules:
                - When the student asks about schedule, lectures, classes, doctors, instructors, rooms, halls, or location, use only the provided schedule context.
                - Mention course code, course name, section, instructor, room, building, campus, time, and location when available.
                - If schedule data is empty, say that no active schedule entries were provided.

                Events rules:
                - Upcoming events include all visible system events/opportunities, even if the student is not registered.
                - Registered events include only events where the student has a registration.
                - If the student asks about available events, use ALL UPCOMING EVENTS AND OPPORTUNITIES.
                - If the student asks "my events" or "registered events", use STUDENT REGISTERED EVENTS.
                - Clearly mention whether the student is registered and whether registration is open when available.
                - Do not say the event does not exist unless it is not present in the provided context.

                Resource and attachment rules:
                - Resources may come from the student's personal library or the system Resource Hub.
                - Notes and tasks may have attachments.
                - File content previews are temporary and should be used only for answering the current question.
                - If the user asks for explanation, summary, quiz, reading, or questions from a file, use ATTACHED FILE CONTENTS first.
                - If file content is not provided, say that the file content was not available in context.

                Grade analysis rules:
                - If multiple grade calculations are provided, use the calculation title/name to identify the target calculation.
                - If the student asks about grades without specifying the calculation name, ask which grade calculation they mean and list the available calculation titles.
                - Do not choose a calculation by course name unless the student clearly asks about a specific course inside a calculation.
                - When analyzing grades, explain GPA, percentage, weak courses, strong courses, repeated courses, and improvement advice.
                - If the student asks "which grade" or "which mark", ask for the grade calculation title/name first.

                Conversation rules:
                - Use only the provided student data and the current conversation history.
                - Do not assume access to other conversations.
                - Do not mix quizzes, answers, tasks, notes, resources, or files from other conversations.

                Formatting rules:
                - Use Markdown when helpful.
                - Use tables only when they make the answer clearer.
                - Use fenced code blocks for code.
                - For quizzes, use clear numbered questions and include answers only if the user asks for answers.
                - For schedules or comparisons, tables are allowed.
                - Do not return raw JSON unless the user explicitly asks.
                - Never expose technical JSON.
                - Never mention internal backend structures.
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
        appendFileContents(sb, context);
        appendEvents(sb, context);
        appendGrades(sb, context);

        sb.append("\n");
        sb.append("=== USER QUESTION ===\n");
        sb.append(nullSafe(userQuestion));

        return sb.toString();
    }

    private void appendStudentProfile(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null || context.getStudent() == null) {
            return;
        }

        sb.append("=== STUDENT ===\n");

        sb.append("Name: ")
                .append(nullSafe(context.getStudent().getFullName()))
                .append("\n");

        sb.append("Email: ")
                .append(nullSafe(context.getStudent().getEmail()))
                .append("\n");

        sb.append("Major: ")
                .append(nullSafe(context.getStudent().getUniversityMajor()))
                .append("\n");

        sb.append("Academic Level: ")
                .append(nullSafe(context.getStudent().getAcademicLevel()))
                .append("\n");

        sb.append("Institution: ")
                .append(nullSafe(context.getStudent().getInstitutionName()))
                .append("\n\n");
    }

    private void appendDashboard(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null) {
            return;
        }

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
                .append(nullSafe(dashboard.getActiveScheduleName()))
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
        if (context == null ||
                context.getTodaySchedule() == null ||
                context.getTodaySchedule().isEmpty()) {
            return;
        }

        sb.append("=== TODAY SCHEDULE ===\n");

        for (ScheduleEntryAiContext entry : context.getTodaySchedule()) {
            appendScheduleEntry(sb, entry);
        }

        sb.append("\n");
    }

    private void appendUpcomingSchedule(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null ||
                context.getUpcomingSchedule() == null ||
                context.getUpcomingSchedule().isEmpty()) {
            return;
        }

        sb.append("=== UPCOMING SCHEDULE ===\n");

        for (ScheduleEntryAiContext entry : context.getUpcomingSchedule()) {
            appendScheduleEntry(sb, entry);
        }

        sb.append("\n");
    }

    private void appendScheduleEntry(
            StringBuilder sb,
            ScheduleEntryAiContext entry
    ) {
        if (entry == null) {
            return;
        }

        sb.append("- Title: ")
                .append(nullSafe(entry.getTitle()))
                .append("\n");

        sb.append("  Time: ")
                .append(formatDate(entry.getStartDateTime()))
                .append(" -> ")
                .append(formatDate(entry.getEndDateTime()))
                .append("\n");

        sb.append("  Course: ")
                .append(nullSafe(entry.getCourseCode()))
                .append(" - ")
                .append(nullSafe(entry.getCourseName()))
                .append("\n");

        sb.append("  Section: ")
                .append(nullSafe(entry.getSectionNumber()))
                .append("\n");

        sb.append("  Instructor: ")
                .append(nullSafe(entry.getInstructorName()))
                .append("\n");

        sb.append("  Room: ")
                .append(nullSafe(entry.getRoom()))
                .append("\n");

        sb.append("  Building: ")
                .append(nullSafe(entry.getBuilding()))
                .append("\n");

        sb.append("  Campus: ")
                .append(nullSafe(entry.getCampus()))
                .append("\n");

        sb.append("  Location: ")
                .append(nullSafe(entry.getLocation()))
                .append("\n");

        sb.append("  Source Type: ")
                .append(nullSafe(entry.getSourceType()))
                .append("\n");
    }

    private void appendTasks(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null) {
            return;
        }

        appendTaskGroup(sb, "ACTIVE TASKS", context.getActiveTasks());
        appendTaskGroup(sb, "OVERDUE TASKS", context.getOverdueTasks());
        appendTaskGroup(sb, "DUE TODAY TASKS", context.getDueTodayTasks());
    }

    private void appendTaskGroup(
            StringBuilder sb,
            String title,
            List<TaskAiContext> tasks
    ) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        sb.append("=== ").append(title).append(" ===\n");

        for (TaskAiContext task : tasks) {
            if (task == null) {
                continue;
            }

            sb.append("- Title: ")
                    .append(nullSafe(task.getTitle()))
                    .append("\n");

            sb.append("  Description: ")
                    .append(limitText(task.getDescription(), 900))
                    .append("\n");

            sb.append("  Course: ")
                    .append(nullSafe(task.getCourseCode()))
                    .append(" - ")
                    .append(nullSafe(task.getCourseName()))
                    .append("\n");

            sb.append("  Priority: ")
                    .append(nullSafe(task.getPriority()))
                    .append("\n");

            sb.append("  Status: ")
                    .append(nullSafe(task.getStatus()))
                    .append("\n");

            sb.append("  Due: ")
                    .append(formatDate(task.getDueDateTime()))
                    .append("\n");

            sb.append("  Estimated Minutes: ")
                    .append(task.getEstimatedMinutes())
                    .append("\n");

            if (task.getLabels() != null && !task.getLabels().isEmpty()) {
                sb.append("  Labels: ")
                        .append(String.join(", ", task.getLabels()))
                        .append("\n");
            }

            if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
                sb.append("  Subtasks:\n");

                for (var subtask : task.getSubtasks()) {
                    if (subtask == null) {
                        continue;
                    }

                    sb.append("    - ")
                            .append(nullSafe(subtask.getTitle()))
                            .append(" | Completed: ")
                            .append(subtask.getCompleted())
                            .append("\n");
                }
            }

            appendAttachments(sb, task.getAttachments());
        }

        sb.append("\n");
    }

    private void appendNotes(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null ||
                context.getImportantNotes() == null ||
                context.getImportantNotes().isEmpty()) {
            return;
        }

        sb.append("=== NOTES ===\n");

        for (NoteAiContext note : context.getImportantNotes()) {
            if (note == null) {
                continue;
            }

            sb.append("- Title: ")
                    .append(nullSafe(note.getTitle()))
                    .append("\n");

            sb.append("  Course: ")
                    .append(nullSafe(note.getCourseCode()))
                    .append(" - ")
                    .append(nullSafe(note.getCourseName()))
                    .append("\n");

            sb.append("  Type: ")
                    .append(nullSafe(note.getNoteType()))
                    .append("\n");

            sb.append("  Priority: ")
                    .append(nullSafe(note.getPriority()))
                    .append("\n");

            sb.append("  Important: ")
                    .append(note.getImportant())
                    .append("\n");

            sb.append("  Pinned: ")
                    .append(note.getPinned())
                    .append("\n");

            sb.append("  Tags: ")
                    .append(nullSafe(note.getTags()))
                    .append("\n");

            if (note.getContentText() != null && !note.getContentText().isBlank()) {
                sb.append("  Content: ")
                        .append(limitText(note.getContentText(), 1200))
                        .append("\n");
            }

            appendAttachments(sb, note.getAttachments());
        }

        sb.append("\n");
    }

    private void appendResources(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null ||
                context.getResources() == null ||
                context.getResources().isEmpty()) {
            return;
        }

        sb.append("=== RESOURCES ===\n");

        for (ResourceAiContext resource : context.getResources()) {
            if (resource == null) {
                continue;
            }

            sb.append("- Title: ")
                    .append(nullSafe(resource.getTitle()))
                    .append("\n");

            sb.append("  Description: ")
                    .append(limitText(resource.getDescription(), 700))
                    .append("\n");

            sb.append("  Type: ")
                    .append(nullSafe(resource.getResourceType()))
                    .append("\n");

            sb.append("  Category: ")
                    .append(nullSafe(resource.getCategory()))
                    .append("\n");

            sb.append("  Course: ")
                    .append(nullSafe(resource.getCourseCode()))
                    .append(" - ")
                    .append(nullSafe(resource.getCourseName()))
                    .append("\n");

            sb.append("  File: ")
                    .append(nullSafe(resource.getFileName()))
                    .append("\n");

            sb.append("  Mime Type: ")
                    .append(nullSafe(resource.getMimeType()))
                    .append("\n");

            sb.append("  File Size Bytes: ")
                    .append(resource.getFileSizeBytes())
                    .append("\n");

            sb.append("  Has File: ")
                    .append(resource.getHasFile())
                    .append("\n");

            sb.append("  Has External Link: ")
                    .append(resource.getHasExternalLink())
                    .append("\n");

            sb.append("  Folder: ")
                    .append(nullSafe(resource.getFolderName()))
                    .append("\n");

            sb.append("  Linked Note ID: ")
                    .append(resource.getLinkedNoteId())
                    .append("\n");

            sb.append("  Linked Task ID: ")
                    .append(resource.getLinkedTaskId())
                    .append("\n");
        }

        sb.append("\n");
    }

    private void appendFileContents(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null ||
                context.getFileContents() == null ||
                context.getFileContents().isEmpty()) {
            return;
        }

        sb.append("=== ATTACHED FILE CONTENTS ===\n");

        for (AiFileContentContext file : context.getFileContents()) {
            if (file == null) {
                continue;
            }

            sb.append("- Source: ")
                    .append(nullSafe(file.getOwnerType()))
                    .append(" | Owner ID: ")
                    .append(file.getOwnerId())
                    .append(" | Title: ")
                    .append(nullSafe(file.getTitle()))
                    .append("\n");

            sb.append("  File: ")
                    .append(nullSafe(file.getFileName()))
                    .append("\n");

            sb.append("  Mime: ")
                    .append(nullSafe(file.getMimeType()))
                    .append("\n");

            sb.append("  Size Bytes: ")
                    .append(file.getFileSizeBytes())
                    .append("\n");

            if (file.getContentPreview() != null &&
                    !file.getContentPreview().isBlank()) {
                sb.append("  Content Preview:\n")
                        .append(limitText(file.getContentPreview(), 5000))
                        .append("\n");
            }
        }

        sb.append("\n");
    }

    private void appendEvents(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null) {
            return;
        }

        if (context.getUpcomingEvents() != null &&
                !context.getUpcomingEvents().isEmpty()) {

            sb.append("=== ALL UPCOMING EVENTS AND OPPORTUNITIES ===\n");

            for (EventAiContext event : context.getUpcomingEvents()) {
                appendEvent(sb, event);
            }

            sb.append("\n");
        }

        if (context.getRegisteredEvents() != null &&
                !context.getRegisteredEvents().isEmpty()) {

            sb.append("=== STUDENT REGISTERED EVENTS ===\n");

            for (EventAiContext event : context.getRegisteredEvents()) {
                appendEvent(sb, event);
            }

            sb.append("\n");
        }
    }

    private void appendEvent(
            StringBuilder sb,
            EventAiContext event
    ) {
        if (event == null) {
            return;
        }

        sb.append("- ID: ")
                .append(event.getId())
                .append("\n");

        sb.append("  Title: ")
                .append(nullSafe(event.getTitle()))
                .append("\n");

        sb.append("  Type: ")
                .append(nullSafe(event.getEventType()))
                .append("\n");

        sb.append("  Time: ")
                .append(formatDate(event.getStartDateTime()))
                .append(" -> ")
                .append(formatDate(event.getEndDateTime()))
                .append("\n");

        sb.append("  Location: ")
                .append(nullSafe(event.getLocation()))
                .append("\n");

        sb.append("  Department: ")
                .append(nullSafe(event.getDepartment()))
                .append("\n");

        sb.append("  Host: ")
                .append(nullSafe(event.getHost()))
                .append("\n");

        sb.append("  Requires Registration: ")
                .append(event.getRequiresRegistration())
                .append("\n");

        sb.append("  Registration Open: ")
                .append(event.getRegistrationOpen())
                .append("\n");

        sb.append("  Student Registered: ")
                .append(event.getRegistered())
                .append("\n");

        sb.append("  Registration Status: ")
                .append(nullSafe(event.getRegistrationStatus()))
                .append("\n");

        sb.append("  Max Participants: ")
                .append(event.getMaxParticipants())
                .append("\n");

        sb.append("  Registered Count: ")
                .append(event.getRegisteredCount())
                .append("\n");

        sb.append("  Opportunity: ")
                .append(event.getIsOpportunity())
                .append("\n");

        if (Boolean.TRUE.equals(event.getIsOpportunity())) {
            sb.append("  Company: ")
                    .append(nullSafe(event.getCompanyName()))
                    .append("\n");

            sb.append("  Role: ")
                    .append(nullSafe(event.getRoleTitle()))
                    .append("\n");

            sb.append("  Field: ")
                    .append(nullSafe(event.getField()))
                    .append("\n");

            sb.append("  Work Mode: ")
                    .append(nullSafe(event.getWorkMode()))
                    .append("\n");

            sb.append("  Paid: ")
                    .append(event.getIsPaid())
                    .append("\n");

            sb.append("  Application Deadline: ")
                    .append(event.getApplicationDeadline())
                    .append("\n");

            sb.append("  Duration Weeks: ")
                    .append(event.getDurationWeeks())
                    .append("\n");
        }

        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            sb.append("  Description: ")
                    .append(limitText(event.getDescription(), 900))
                    .append("\n");
        }
    }

    private void appendGrades(
            StringBuilder sb,
            StudentAiContextDto context
    ) {
        if (context == null) {
            return;
        }

        if (context.getGradeCalculations() != null &&
                !context.getGradeCalculations().isEmpty()) {

            sb.append("=== GRADE CALCULATIONS ===\n");

            for (GradeAiContext grades : context.getGradeCalculations()) {
                appendGradeCalculation(sb, grades);
            }

            sb.append("\n");
            return;
        }

        GradeAiContext grades = context.getGrades();

        if (grades == null) {
            return;
        }

        sb.append("=== LATEST GRADES ===\n");
        appendGradeCalculation(sb, grades);
        sb.append("\n");
    }

    private void appendGradeCalculation(
            StringBuilder sb,
            GradeAiContext grades
    ) {
        if (grades == null) {
            return;
        }

        sb.append("- Calculation ID: ")
                .append(grades.getCalculationId())
                .append("\n");

        sb.append("  Title: ")
                .append(nullSafe(grades.getTitle()))
                .append("\n");

        sb.append("  Type: ")
                .append(nullSafe(grades.getCalculationType()))
                .append("\n");

        sb.append("  Input Type: ")
                .append(nullSafe(grades.getInputType()))
                .append("\n");

        sb.append("  Repeat Policy: ")
                .append(nullSafe(grades.getRepeatPolicy()))
                .append("\n");

        sb.append("  GPA: ")
                .append(grades.getCalculatedGpa())
                .append("\n");

        sb.append("  Percentage: ")
                .append(grades.getCalculatedPercentage())
                .append("\n");

        sb.append("  Total Credits: ")
                .append(grades.getTotalCredits())
                .append("\n");

        sb.append("  Subject Count: ")
                .append(grades.getSubjectCount())
                .append("\n");

        if (grades.getCourses() != null && !grades.getCourses().isEmpty()) {
            sb.append("  Courses:\n");

            for (GradeCourseAiContext course : grades.getCourses()) {
                if (course == null) {
                    continue;
                }

                sb.append("    - ID: ")
                        .append(course.getId())
                        .append(" | ")
                        .append(nullSafe(course.getCourseCode()))
                        .append(" | ")
                        .append(nullSafe(course.getCourseName()))
                        .append(" | Credits: ")
                        .append(course.getCreditHours())
                        .append(" | Entered: ")
                        .append(course.getEnteredValue())
                        .append(" | Percentage: ")
                        .append(course.getNormalizedPercentage())
                        .append(" | Letter: ")
                        .append(nullSafe(course.getLetterGrade()))
                        .append(" | Points: ")
                        .append(course.getGradePoints())
                        .append(" | Status: ")
                        .append(nullSafe(course.getStatus()))
                        .append(" | Repeated: ")
                        .append(course.getRepeatedCourse())
                        .append(" | Included: ")
                        .append(course.getIncludedInCalculation())
                        .append("\n");
            }
        }
    }

    private void appendAttachments(
            StringBuilder sb,
            List<AttachmentAiContext> attachments
    ) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        sb.append("  Attachments:\n");

        for (AttachmentAiContext attachment : attachments) {
            if (attachment == null) {
                continue;
            }

            sb.append("    - ID: ")
                    .append(attachment.getId())
                    .append(" | File: ")
                    .append(nullSafe(attachment.getFileName()))
                    .append(" | Type: ")
                    .append(nullSafe(attachment.getType()))
                    .append(" | Mime: ")
                    .append(nullSafe(attachment.getMimeType()))
                    .append(" | Size: ")
                    .append(attachment.getFileSizeBytes())
                    .append(" | Voice Note: ")
                    .append(attachment.getIsVoiceNote())
                    .append(" | Duration Seconds: ")
                    .append(attachment.getDurationSeconds())
                    .append("\n");
        }
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

    private String limitText(String text, int maxChars) {
        if (text == null) {
            return "-";
        }

        String clean = text.trim();

        if (clean.isBlank()) {
            return "-";
        }

        if (clean.length() <= maxChars) {
            return clean;
        }

        return clean.substring(0, maxChars) + "...";
    }
}