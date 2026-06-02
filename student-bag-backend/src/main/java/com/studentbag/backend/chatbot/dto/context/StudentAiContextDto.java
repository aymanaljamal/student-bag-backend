package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAiContextDto {

    private StudentProfileContext student;

    private DashboardAiContext dashboard;

    private List<ScheduleEntryAiContext> todaySchedule;
    private List<ScheduleEntryAiContext> upcomingSchedule;

    private List<TaskAiContext> activeTasks;
    private List<TaskAiContext> overdueTasks;
    private List<TaskAiContext> dueTodayTasks;

    private List<NoteAiContext> importantNotes;
    private List<ResourceAiContext> resources;

    /*
     * Temporary extracted file contents for AI only.
     *
     * This is NOT stored in database.
     * It is filled only when the student asks something that needs file reading,
     * such as quiz generation, explanation, summary, or reading attachments.
     *
     * Sources can be:
     * - RESOURCE
     * - NOTE_ATTACHMENT
     * - TASK_ATTACHMENT
     */
    private List<AiFileContentContext> fileContents;

    private List<EventAiContext> upcomingEvents;

    private GradeAiContext grades;
}