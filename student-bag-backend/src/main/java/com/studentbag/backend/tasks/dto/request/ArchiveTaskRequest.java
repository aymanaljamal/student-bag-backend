package com.studentbag.backend.tasks.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveTaskRequest {

    @Builder.Default
    private Boolean archived = true;
}