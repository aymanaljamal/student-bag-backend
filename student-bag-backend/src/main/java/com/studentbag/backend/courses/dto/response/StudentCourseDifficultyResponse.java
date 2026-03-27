package com.studentbag.backend.courses.dto.response;

import com.studentbag.backend.domain.enums.CourseDifficulty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudentCourseDifficultyResponse {

    private Long id;
    private Long studentId;
    private Long courseId;
    private CourseDifficulty difficulty;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}