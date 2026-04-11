package com.studentbag.backend.tasks.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubtaskRequest {

    private Long id;

    private String title;

    private Boolean isCompleted;

    private Integer orderIndex;
}