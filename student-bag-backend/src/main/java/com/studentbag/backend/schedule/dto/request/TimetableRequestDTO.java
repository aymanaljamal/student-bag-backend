package com.studentbag.backend.schedule.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TimetableRequestDTO extends PreferenceRequestDTO {

    @NotNull(message = "يجب تحديد الفصل الدراسي")
    private Long termId;

    @Valid
    private List<CourseRatingRequestDTO> courseRatings;

    @NotEmpty(message = "يجب اختيار مساق واحد على الأقل")
    private List<Long> courseIds;

    private List<Long> lockedSectionIds;
}