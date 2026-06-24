package com.studentbag.backend.courses.sync.dto;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class RitajClassSessionDto {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    private String room;
    private String building;
    private String campus;

    private String sessionInternalId;
    private Boolean isGeneratedSession;
}
