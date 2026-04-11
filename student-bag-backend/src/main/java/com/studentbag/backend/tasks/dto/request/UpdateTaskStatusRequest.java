package com.studentbag.backend.tasks.dto.request;


import com.studentbag.backend.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskStatusRequest {

    @NotNull
    private TaskStatus status;
}