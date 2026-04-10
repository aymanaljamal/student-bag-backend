package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;

import java.util.List;
import java.util.Map;

public interface TimetableGeneratorService {

    List<ScheduleOptionResponseDTO> generateValidOptions(
            Long termId,
            List<Long> courseIds,
            List<Long> lockedSectionIds,
            Map<Long, Integer> courseRatings,
            Long studentId
    );
}