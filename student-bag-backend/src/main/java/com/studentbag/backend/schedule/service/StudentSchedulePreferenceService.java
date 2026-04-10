package com.studentbag.backend.schedule.service;

import com.studentbag.backend.schedule.dto.request.TimetableRequestDTO;

public interface StudentSchedulePreferenceService {
    void savePreferences(Long studentId, TimetableRequestDTO request);
}