package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import java.util.List;

public interface ScheduleManagementService {

    void activateSchedule(Long scheduleId, Long studentId);

    // FIX: Returns a List of DTOs instead of Entities
    List<StudentScheduleResponseDTO> getStudentSchedules(Long studentId);

    // FIX: Added studentId to the signature for security validation
    void deleteSchedule(Long scheduleId, Long studentId);
}