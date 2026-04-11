package com.studentbag.backend.tasks.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSearchMatchResponse {

    /**
     * TITLE, DESCRIPTION, SUBTASK, ATTACHMENT_FILE_NAME
     */
    private String matchType;

    private String matchedValue;
}