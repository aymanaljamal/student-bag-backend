package com.studentbag.backend.schedule.dto.request;

import com.studentbag.backend.domain.enums.ScheduleSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateScheduleEntryRequest {

    @NotBlank
    private String title;

    private String description;
    private String location;

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    private Boolean isAllDay = false;
    private String colorHex;

    @NotNull
    private ScheduleSourceType sourceType;

    // إذا COURSE
    private Long courseSectionId;

    // إذا EVENT
    private Long eventId;
}