package com.studentbag.backend.schedule.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateManualScheduleRequest(
        @NotBlank(message = "Schedule name is required")
        String name,

        @NotEmpty(message = "At least one schedule entry is required")
        List<@Valid ManualScheduleEntryRequest> entries
) {
}