package com.studentbag.backend.tasks.dto.response;


import com.studentbag.backend.domain.enums.TaskPriority;
import com.studentbag.backend.domain.enums.TaskRecurrenceType;
import com.studentbag.backend.domain.enums.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private TaskPriority priority;
    private TaskStatus status;

    private Boolean archived;
    private Boolean deleted;
    private Boolean completed;
    private Boolean overdue;

    private LocalDateTime completedAt;
    private Integer estimatedMinutes;

    private TaskRecurrenceType recurrenceType;
    private Integer recurrenceInterval;
    private LocalDateTime recurrenceLastGeneratedAt;
    private LocalDateTime nextOccurrenceAt;

    private Long studentId;

    private Long courseId;
    private String courseCode;
    private String courseNameArabic;
    private String courseNameEnglish;

    private List<TaskLabelResponse> labels;
    private List<SubtaskResponse> subtasks;
    private List<TaskAttachmentResponse> attachments;
    private List<TaskReminderResponse> reminders;

    private Integer subtaskCount;
    private Integer completedSubtaskCount;
    private Integer attachmentCount;
    private Integer reminderCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}