package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.schedule.dto.request.CreateManualScheduleRequest;
import com.studentbag.backend.schedule.dto.request.ManualScheduleEntryRequest;
import com.studentbag.backend.schedule.dto.request.UpdateManualScheduleNameRequest;
import com.studentbag.backend.schedule.dto.response.ManualCourseOptionDto;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.service.ManualScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/schedules/manual")
@RequiredArgsConstructor
public class ManualScheduleController {

    private final ManualScheduleService manualScheduleService;

    @GetMapping("/course-options")
    public List<ManualCourseOptionDto> getCourseOptions() {
        return manualScheduleService.getCourseOptionsForCurrentStudent();
    }

    @PostMapping
    public StudentScheduleResponseDTO createManualSchedule(
            @Valid @RequestBody CreateManualScheduleRequest request
    ) {
        return manualScheduleService.createManualSchedule(request);
    }

    @GetMapping("/{scheduleId}")
    public StudentScheduleResponseDTO getManualSchedule(
            @PathVariable Long scheduleId
    ) {
        return manualScheduleService.getManualSchedule(scheduleId);
    }

    @PutMapping("/{scheduleId}/name")
    public StudentScheduleResponseDTO updateScheduleName(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateManualScheduleNameRequest request
    ) {
        return manualScheduleService.updateScheduleName(scheduleId, request);
    }

    @PostMapping("/{scheduleId}/entries")
    public StudentScheduleResponseDTO addEntry(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ManualScheduleEntryRequest request
    ) {
        return manualScheduleService.addEntry(scheduleId, request);
    }

    @PutMapping("/{scheduleId}/entries/{entryId}")
    public StudentScheduleResponseDTO updateEntry(
            @PathVariable Long scheduleId,
            @PathVariable Long entryId,
            @Valid @RequestBody ManualScheduleEntryRequest request
    ) {
        return manualScheduleService.updateEntry(scheduleId, entryId, request);
    }

    @DeleteMapping("/{scheduleId}/entries/{entryId}")
    public StudentScheduleResponseDTO deleteEntry(
            @PathVariable Long scheduleId,
            @PathVariable Long entryId
    ) {
        return manualScheduleService.deleteEntry(scheduleId, entryId);
    }
}