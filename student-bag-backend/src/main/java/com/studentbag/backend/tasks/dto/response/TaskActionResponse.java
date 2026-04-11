package com.studentbag.backend.tasks.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskActionResponse {

    private Long taskId;
    private String action;
    private String message;
    private Boolean success;
}