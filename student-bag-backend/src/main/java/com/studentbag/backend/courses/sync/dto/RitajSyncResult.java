package com.studentbag.backend.courses.sync.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RitajSyncResult {
    private int facultiesCreated;
    private int departmentsCreated;
    private int coursesCreated;
    private int coursesUpdated;
    private int instructorsCreated;
    private int sectionsCreated;
    private int sectionsUpdated;
    private int sessionsCreated;
}