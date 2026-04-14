package com.studentbag.backend.tasks.dto.response;

import com.studentbag.backend.domain.enums.tasks.TaskPriority;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private TaskPriority priority;
    private TaskStatus status;

    private Boolean archived;
    private Boolean completed;
    private Boolean overdue;

    private Integer estimatedMinutes;
    private Integer totalSubtasks;
    private Integer completedSubtasks;
    private Integer attachmentCount;
    private Integer reminderCount;

    private TaskCourseSummaryResponse course;
    private List<TaskLabelResponse> labels;

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}