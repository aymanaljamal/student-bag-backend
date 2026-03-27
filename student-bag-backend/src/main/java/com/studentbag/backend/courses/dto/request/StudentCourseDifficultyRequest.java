package com.studentbag.backend.courses.dto.request;

import com.studentbag.backend.domain.enums.CourseDifficulty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentCourseDifficultyRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long courseId;

    @NotNull
    private CourseDifficulty difficulty;

    private String note;
}