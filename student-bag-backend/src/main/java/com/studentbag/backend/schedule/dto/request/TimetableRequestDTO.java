package com.studentbag.backend.schedule.dto.request;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableRequestDTO {

    @NotNull(message = "يجب تحديد الفصل الدراسي")
    private Long termId;

    @NotEmpty(message = "يجب اختيار مساق واحد على الأقل")
    private List<Long> courseIds;

    // FR-4.7: قائمة بمعرفات الشعب التي يريد الطالب قفلها (Locked Sections)
    private List<Long> lockedSectionIds;
}