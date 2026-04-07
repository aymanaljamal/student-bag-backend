package com.studentbag.backend.courses.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO for creating/updating ClassSession
 */
@Data
public class ClassSessionRequestDTO {

    @NotNull
    private Long courseSectionId;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private String room;
    private String building;
    private String campus;

    private Boolean isOnline = false;
}