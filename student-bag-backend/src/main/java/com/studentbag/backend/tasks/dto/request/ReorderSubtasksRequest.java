package com.studentbag.backend.tasks.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderSubtasksRequest {

    @NotEmpty
    private List<SubtaskOrderItemRequest> items;
}