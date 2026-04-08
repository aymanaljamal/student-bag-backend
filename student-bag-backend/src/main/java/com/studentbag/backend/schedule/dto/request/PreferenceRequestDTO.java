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

    private Integer maxConsecutiveHours;

    @Min(0) @Max(180)
    private Integer maxGapMinutes;

    private List<String> preferredDaysOff;
}