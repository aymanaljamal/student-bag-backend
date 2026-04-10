package com.studentbag.backend.schedule.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class PreferenceRequestDTO {

    private Boolean avoidEarlyMorning;

    private LocalTime earliestStartTime;

    @Min(value = 1, message = "maxConsecutiveHours must be at least 1")
    @Max(value = 12, message = "maxConsecutiveHours must be at most 12")
    private Integer maxConsecutiveHours;

    @Min(value = 0, message = "maxGapMinutes must be at least 0")
    @Max(value = 180, message = "maxGapMinutes must be at most 180")
    private Integer maxGapMinutes;

    private List<String> preferredDaysOff;
}