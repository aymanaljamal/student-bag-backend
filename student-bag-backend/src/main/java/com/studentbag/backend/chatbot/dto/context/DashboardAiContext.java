package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAiContext {

    private Long activeTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Long dueTodayTasks;

    private Long totalNotes;
    private Long importantNotes;
    private Long pinnedNotes;

    private Boolean hasActiveSchedule;
    private String activeScheduleName;
    private Long todaySessions;

    private Double latestGpa;
    private Double latestPercentage;

    private Long upcomingReminders;
    private Long registeredEvents;
}