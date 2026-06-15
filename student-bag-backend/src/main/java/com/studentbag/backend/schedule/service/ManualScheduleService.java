package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.request.CreateManualScheduleRequest;
import com.studentbag.backend.schedule.dto.request.ManualScheduleEntryRequest;
import com.studentbag.backend.schedule.dto.request.UpdateManualScheduleNameRequest;
import com.studentbag.backend.schedule.dto.response.ManualCourseOptionDto;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;

import java.util.List;

public interface ManualScheduleService {

    List<ManualCourseOptionDto> getCourseOptionsForCurrentStudent();

    StudentScheduleResponseDTO createManualSchedule(
            CreateManualScheduleRequest request
    );

    StudentScheduleResponseDTO getManualSchedule(
            Long scheduleId
    );

    StudentScheduleResponseDTO updateScheduleName(
            Long scheduleId,
            UpdateManualScheduleNameRequest request
    );

    StudentScheduleResponseDTO addEntry(
            Long scheduleId,
            ManualScheduleEntryRequest request
    );

    StudentScheduleResponseDTO updateEntry(
            Long scheduleId,
            Long entryId,
            ManualScheduleEntryRequest request
    );

    StudentScheduleResponseDTO deleteEntry(
            Long scheduleId,
            Long entryId
    );
}