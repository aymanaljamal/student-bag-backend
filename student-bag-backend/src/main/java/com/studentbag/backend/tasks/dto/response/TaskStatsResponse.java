package com.studentbag.backend.tasks.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatsResponse {

    private Long allCount;
    private Long activeCount;
    private Long completedCount;
    private Long archivedCount;
    private Long overdueCount;
    private Long dueTodayCount;
}