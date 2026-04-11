package com.studentbag.backend.tasks.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskReminderPreviewResponse {

    private Long taskId;
    private String taskTitle;
    private LocalDateTime dueDateTime;
    private LocalDateTime remindAt;
    private Integer minutesBefore;
    private Boolean enabled;
}