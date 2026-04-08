package com.studentbag.backend.schedule.service;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;

import java.util.List;

public interface PreferenceRankingService {
    List<ScheduleOptionResponseDTO> rankAndScore(List<List<CourseSection>> options, Long studentId);
}