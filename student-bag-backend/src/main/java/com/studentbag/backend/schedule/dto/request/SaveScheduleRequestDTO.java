package com.studentbag.backend.schedule.dto.request;

import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveScheduleRequestDTO {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "termId is required")
    private Long termId;

    @NotNull(message = "option is required")
    private ScheduleOptionResponseDTO option;
}