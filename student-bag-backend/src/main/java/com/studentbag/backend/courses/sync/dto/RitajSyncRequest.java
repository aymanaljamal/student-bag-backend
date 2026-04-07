package com.studentbag.backend.courses.sync.dto;

import lombok.Data;

@Data
public class RitajSyncRequest {
    private Long institutionId;
    private String sourceUrl;
}