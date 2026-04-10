package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.schedule.dto.request.SaveScheduleRequestDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleViewerResponseDTO;
import com.studentbag.backend.schedule.service.StudentScheduleSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules/save")
@RequiredArgsConstructor
public class StudentScheduleSaveController {

    private final StudentScheduleSaveService saveService;

    @PostMapping
    public ResponseEntity<StudentScheduleResponseDTO> save(
            @RequestBody SaveScheduleRequestDTO request
    ) {
        return ResponseEntity.ok(saveService.saveSelectedSchedule(request));
    }

    @PutMapping("/{scheduleId}/activate")
    public ResponseEntity<StudentScheduleViewerResponseDTO> activateSchedule(
            @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(saveService.activateSchedule(scheduleId));
    }

    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<StudentScheduleViewerResponseDTO> getActiveSchedule(
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(saveService.getActiveSchedule(studentId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentScheduleViewerResponseDTO>> getStudentSchedules(
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(saveService.getStudentSchedules(studentId));
    }
}