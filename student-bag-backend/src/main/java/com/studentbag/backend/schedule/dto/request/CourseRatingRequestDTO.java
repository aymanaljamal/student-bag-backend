package com.studentbag.backend.schedule.dto.request;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRatingRequestDTO {

    @NotNull
    private Long courseId;

    @Min(1) @Max(5)
    private Integer difficultyRating; // 1=سهل، 5=صعب جداً
}