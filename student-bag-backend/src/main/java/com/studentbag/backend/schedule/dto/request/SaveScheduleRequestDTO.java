package com.studentbag.backend.schedule.dto.request;

import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SaveScheduleRequestDTO {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "termId is required")
    private Long termId;

    @NotNull(message = "option is required")
    private ScheduleOptionResponseDTO option;

    /**
     * كل الشعب المختارة من الطالب.
     * مهم عشان نحفظ كورسات ما إلها وقت/entry كـ hidden all-day entries.
     */
    private List<Long> selectedCourseSectionIds = new ArrayList<>();
}