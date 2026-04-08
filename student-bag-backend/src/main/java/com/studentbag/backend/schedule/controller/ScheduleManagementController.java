package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.service.ScheduleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules/management")
@RequiredArgsConstructor
@Tag(name = "Schedule Management", description = "Endpoints for managing active and saved student schedules")
public class ScheduleManagementController {

    private final ScheduleManagementService managementService;

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all schedules for a student", description = "Returns active and archived schedules.")
    public ResponseEntity<List<StudentScheduleResponseDTO>> getMySchedules(@PathVariable Long studentId) {
        return ResponseEntity.ok(managementService.getStudentSchedules(studentId));
    }

    @PutMapping("/{scheduleId}/activate")
    @Operation(summary = "Activate a specific schedule",
            description = "Sets one schedule to ACTIVE and archives all others for the same term (FR-4.8).")
    public ResponseEntity<Void> activate(@PathVariable Long scheduleId, @RequestParam Long studentId) {
        managementService.activateSchedule(scheduleId, studentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "Delete a schedule", description = "Permanently removes a saved schedule.")
    public ResponseEntity<Void> delete(@PathVariable Long scheduleId, @RequestParam Long studentId) {
        managementService.deleteSchedule(scheduleId, studentId);
        return ResponseEntity.noContent().build();
    }
}