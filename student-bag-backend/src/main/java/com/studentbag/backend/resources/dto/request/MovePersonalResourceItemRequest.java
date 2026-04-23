package com.studentbag.backend.resources.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovePersonalResourceItemRequest {

    @NotNull
    private Long targetFolderId;
}