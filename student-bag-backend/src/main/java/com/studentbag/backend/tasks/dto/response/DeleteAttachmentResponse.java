package com.studentbag.backend.tasks.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAttachmentResponse {

    private Long attachmentId;
    private Boolean deleted;
    private String message;
}