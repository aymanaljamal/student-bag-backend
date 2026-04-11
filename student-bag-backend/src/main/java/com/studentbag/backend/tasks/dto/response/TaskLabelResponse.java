package com.studentbag.backend.tasks.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskLabelResponse {

    private Long id;
    private String name;
    private String colorHex;
}