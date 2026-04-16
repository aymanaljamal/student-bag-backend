package com.studentbag.backend.tasks.dto.request;

import com.studentbag.backend.domain.enums.tasks.TaskPriority;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSearchRequest {

    /**
     * بحث عام:
     * title, description, subtask title, attachment fileName
     */
    private String query;

    private TaskStatus status;

    private TaskPriority priority;

    private Long courseId;

    private Set<Long> labelIds;

    private Boolean archived;

    private Boolean completed;

    private Boolean overdue;

    private Boolean dueToday;

    private LocalDate dueFrom;

    private LocalDate dueTo;

    /**
     * sorting:
     * dueDateTime, priority, createdAt, title, course
     */
    private String sortBy;

    /**
     * asc / desc
     */
    private String sortDirection;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}