package com.studentbag.backend.schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConflictDTO {
    private String entryATitle;
    private String entryBTitle;
    private LocalDateTime conflictStart;
    private LocalDateTime conflictEnd;
}