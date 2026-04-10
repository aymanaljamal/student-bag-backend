package com.studentbag.backend.schedule.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRatingRequestDTO {

    @NotNull(message = "courseId is required")
    private Long courseId;

    @NotNull(message = "difficultyRating is required")
    @Min(value = 1, message = "difficultyRating must be at least 1")
    @Max(value = 5, message = "difficultyRating must be at most 5")
    private Integer difficultyRating;
}