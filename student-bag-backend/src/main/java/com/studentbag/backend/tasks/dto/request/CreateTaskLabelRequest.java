package com.studentbag.backend.tasks.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskLabelRequest {

    @NotBlank
    private String name;

    private String colorHex;
}