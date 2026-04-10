package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.schedule.dto.request.CourseRatingRequestDTO;
import com.studentbag.backend.schedule.dto.request.TimetableRequestDTO;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import com.studentbag.backend.schedule.service.CourseRatingService;
import com.studentbag.backend.schedule.service.StudentSchedulePreferenceService;
import com.studentbag.backend.schedule.service.TimetableGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/schedules/generator")
@RequiredArgsConstructor
@Tag(name = "Timetable Generator", description = "Endpoints for generating non-conflicting schedule options")
public class TimetableGeneratorController {

    private final TimetableGeneratorService generatorService;
    private final CourseRatingService courseRatingService;
    private final StudentSchedulePreferenceService preferenceService;

    @PostMapping("/generate")
    @Operation(summary = "Generate valid schedule combinations")
    public ResponseEntity<List<ScheduleOptionResponseDTO>> generateOptions(
            @Valid @RequestBody TimetableRequestDTO request,
            @RequestParam Long studentId
    ) {
        preferenceService.savePreferences(studentId, request);
        courseRatingService.saveRatings(studentId, request.getCourseRatings());

        Map<Long, Integer> courseRatingsMap =
                request.getCourseRatings() == null
                        ? Collections.emptyMap()
                        : request.getCourseRatings().stream()
                        .filter(r -> r.getCourseId() != null && r.getDifficultyRating() != null)
                        .collect(Collectors.toMap(
                                CourseRatingRequestDTO::getCourseId,
                                CourseRatingRequestDTO::getDifficultyRating,
                                (oldValue, newValue) -> newValue
                        ));

        List<ScheduleOptionResponseDTO> options = generatorService.generateValidOptions(
                request.getTermId(),
                request.getCourseIds(),
                request.getLockedSectionIds(),
                courseRatingsMap,
                studentId
        );

        return ResponseEntity.ok(options);
    }
}