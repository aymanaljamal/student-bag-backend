package com.studentbag.backend.tasks.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkTaskActionResponse {

    private String action;
    private Integer requestedCount;
    private Integer successCount;
    private Integer failedCount;
    private List<Long> successTaskIds;
    private List<Long> failedTaskIds;
    private String message;
}