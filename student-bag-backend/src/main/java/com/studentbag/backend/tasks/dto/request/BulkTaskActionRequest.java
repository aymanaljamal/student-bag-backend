package com.studentbag.backend.tasks.dto.request;

import com.studentbag.backend.domain.enums.TaskPriority;
import com.studentbag.backend.domain.enums.TaskStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkTaskActionRequest {

    private List<Long> taskIds;

    /**
     * COMPLETE, UNCOMPLETE, DELETE, ARCHIVE, UNARCHIVE, CHANGE_PRIORITY, CHANGE_STATUS
     */
    private String action;

    private TaskPriority priority;

    private TaskStatus status;
}