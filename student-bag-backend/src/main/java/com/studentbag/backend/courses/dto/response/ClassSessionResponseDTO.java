package com.studentbag.backend.courses.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO for returning ClassSession data
 */
@Data
@Builder
public class ClassSessionResponseDTO {

    private Long id;
    private Long courseSectionId;

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    private String room;
    private String building;
    private String campus;

    private Boolean isOnline;

    private int durationMinutes;
}