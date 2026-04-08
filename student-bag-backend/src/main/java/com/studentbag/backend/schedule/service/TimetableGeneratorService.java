package com.studentbag.backend.schedule.service;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import java.util.List;
public interface TimetableGeneratorService {
    List<ScheduleOptionResponseDTO> generateValidOptions(Long termId, List<Long> courseIds, List<Long> lockedSectionIds, Long studentId);
}