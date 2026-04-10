package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.schedule.dto.request.TimetableRequestDTO;
import com.studentbag.backend.schedule.service.StudentSchedulePreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedules/preferences")
@RequiredArgsConstructor
public class StudentSchedulePreferenceController {

    private final StudentSchedulePreferenceService preferenceService;

    @PostMapping
    public ResponseEntity<Void> savePreferences(
            @RequestParam Long studentId,
            @RequestBody TimetableRequestDTO request
    ) {
        preferenceService.savePreferences(studentId, request);
        return ResponseEntity.noContent().build();
    }
}