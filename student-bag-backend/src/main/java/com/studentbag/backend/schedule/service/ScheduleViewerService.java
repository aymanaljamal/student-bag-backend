package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.response.StudentScheduleViewerResponseDTO;
import java.util.List;

public interface ScheduleViewerService {
    List<StudentScheduleViewerResponseDTO> getStudentSchedulesViewer(Long studentId);
    StudentScheduleViewerResponseDTO getScheduleViewer(Long scheduleId, Long studentId); // ← أضف لو ناقص
}