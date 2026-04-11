package com.studentbag.backend.tasks.dto.request;

import com.studentbag.backend.domain.enums.TaskStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatusChangeRequest {

    private TaskStatus status;
}