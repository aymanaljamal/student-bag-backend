package com.studentbag.backend.tasks.dto.request;

import com.studentbag.backend.domain.enums.TaskPriority;
import com.studentbag.backend.domain.enums.TaskRecurrenceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    private LocalDateTime dueDateTime;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private Long courseId;

    @Builder.Default
    private Set<Long> labelIds = Set.of();

    @Builder.Default
    private Integer estimatedMinutes = 0;

    @Builder.Default
    private TaskRecurrenceType recurrenceType = TaskRecurrenceType.NONE;

    @Builder.Default
    private Integer recurrenceInterval = 1;

    @Valid
    @Builder.Default
    private List<CreateSubtaskRequest> subtasks = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<CreateTaskReminderRequest> reminders = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<CreateTaskAttachmentRequest> attachments = new ArrayList<>();
}