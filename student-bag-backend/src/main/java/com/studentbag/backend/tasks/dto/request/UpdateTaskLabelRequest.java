package com.studentbag.backend.tasks.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskLabelRequest {

    private String name;

    private String colorHex;
}