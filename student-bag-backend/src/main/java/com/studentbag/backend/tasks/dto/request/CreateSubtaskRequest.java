package com.studentbag.backend.tasks.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubtaskRequest {

    @NotBlank
    private String title;

    @Builder.Default
    private Boolean isCompleted = false;

    @Builder.Default
    private Integer orderIndex = 0;
}