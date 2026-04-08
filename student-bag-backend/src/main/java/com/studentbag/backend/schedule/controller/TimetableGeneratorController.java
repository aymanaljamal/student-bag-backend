package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.schedule.dto.request.TimetableRequestDTO;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import com.studentbag.backend.schedule.service.TimetableGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules/generator")
@RequiredArgsConstructor
@Tag(name = "Timetable Generator", description = "Endpoints for generating non-conflicting schedule options")
public class TimetableGeneratorController {

    private final TimetableGeneratorService generatorService;

    @PostMapping("/generate")
    @Operation(summary = "Generate valid schedule combinations",
            description = "Uses backtracking to find all non-conflicting combinations and ranks them by preference.")
    public ResponseEntity<List<ScheduleOptionResponseDTO>> generateOptions(
            @Valid @RequestBody TimetableRequestDTO request,
            @RequestParam Long studentId) { // In production, get studentId from JWT token

        List<ScheduleOptionResponseDTO> options = generatorService.generateValidOptions(
                request.getTermId(),
                request.getCourseIds(),
                request.getLockedSectionIds(),
                studentId);

        return ResponseEntity.ok(options);
    }
}