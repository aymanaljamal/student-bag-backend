package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.request.SaveScheduleRequestDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleViewerResponseDTO;


import java.util.List;

public interface StudentScheduleSaveService {

    StudentScheduleResponseDTO saveSelectedSchedule(SaveScheduleRequestDTO request);

    StudentScheduleViewerResponseDTO activateSchedule(Long scheduleId);

    StudentScheduleViewerResponseDTO getActiveSchedule(Long studentId);

    List<StudentScheduleViewerResponseDTO> getStudentSchedules(Long studentId);
}