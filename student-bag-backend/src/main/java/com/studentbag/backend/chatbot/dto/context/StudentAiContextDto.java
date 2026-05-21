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

    private List<EventAiContext> upcomingEvents;

    private GradeAiContext grades;
}