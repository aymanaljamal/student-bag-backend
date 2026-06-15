package com.studentbag.backend.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateManualScheduleNameRequest(
        @NotBlank(message = "Schedule name is required")
        String name
) {
}