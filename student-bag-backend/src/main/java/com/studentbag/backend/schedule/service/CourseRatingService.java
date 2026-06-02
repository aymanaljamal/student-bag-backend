package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.request.CourseRatingRequestDTO;

import java.util.List;

public interface CourseRatingService {

    void saveRatings(Long studentId, List<CourseRatingRequestDTO> ratings);

    Double getAverageDifficulty(Long courseId);
}