package com.studentbag.backend.tasks.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtaskOrderItemRequest {

    @NotNull
    private Long subtaskId;

    @NotNull
    private Integer orderIndex;
}