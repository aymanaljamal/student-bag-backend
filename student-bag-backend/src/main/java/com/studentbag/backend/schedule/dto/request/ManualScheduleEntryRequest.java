package com.studentbag.backend.schedule.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ManualScheduleEntryRequest(
        @NotNull(message = "Course is required")
        Long courseId,

        @NotNull(message = "Day is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        String sectionNumber,
        String room,
        String building,
        String note
) {
}