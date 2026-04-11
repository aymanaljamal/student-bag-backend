package com.studentbag.backend.tasks.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAdvancedSearchItemResponse {

    private TaskSummaryResponse task;
    private List<TaskSearchMatchResponse> matches;
}