package com.studentbag.backend.resources.dto.request;

import lombok.Data;

@Data
public class GenerateFoldersFromActiveScheduleRequest {
    private Long termId;
    private Boolean overwriteExisting;
}