package com.studentbag.backend.tasks.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAttachmentUploadResponse {

    private Long taskId;
    private TaskAttachmentResponse attachment;
    private String message;
}