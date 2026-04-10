package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.schedule.dto.request.CourseRatingRequestDTO;
import com.studentbag.backend.schedule.service.CourseRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules/ratings")
@RequiredArgsConstructor
public class CourseRatingController {

    private final CourseRatingService courseRatingService;

    @PostMapping
    public ResponseEntity<Void> saveRatings(
            @RequestParam Long studentId,
            @RequestBody List<CourseRatingRequestDTO> ratings
    ) {
        courseRatingService.saveRatings(studentId, ratings);
        return ResponseEntity.noContent().build();
    }
}