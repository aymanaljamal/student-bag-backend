package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.request.UpdateScheduleRequest;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.dto.response.UpdateScheduleResponseDTO;

import java.util.List;

public interface ScheduleManagementService {
    void activateSchedule(Long scheduleId, Long studentId);
    void archiveSchedule(Long scheduleId, Long studentId);
    List<StudentScheduleResponseDTO> getStudentSchedules(Long studentId);
    void deleteSchedule(Long scheduleId, Long studentId);
    UpdateScheduleResponseDTO updateScheduleEntries(
                                                        Long scheduleId,
                                                        Long studentId,
                                                        UpdateScheduleRequest request
    );
}