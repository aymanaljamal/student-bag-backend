package com.studentbag.backend.tasks.dto.request;


import com.studentbag.backend.domain.enums.TaskPriority;
import com.studentbag.backend.domain.enums.TaskRecurrenceType;
import jakarta.validation.Valid;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    private String title;

    private String description;

    private LocalDateTime dueDateTime;

    private TaskPriority priority;

    private Long courseId;

    private Set<Long> labelIds;

    private Integer estimatedMinutes;

    private TaskRecurrenceType recurrenceType;

    private Integer recurrenceInterval;

    @Valid
    private List<UpdateSubtaskRequest> subtasks;

    @Valid
    private List<UpdateTaskReminderRequest> reminders;
}