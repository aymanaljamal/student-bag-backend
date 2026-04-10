package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.schedule.dto.request.TimetableRequestDTO;
import com.studentbag.backend.schedule.entity.StudentSchedulePreference;
import com.studentbag.backend.schedule.repository.StudentSchedulePreferenceRepository;
import com.studentbag.backend.schedule.service.StudentSchedulePreferenceService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentSchedulePreferenceServiceImpl implements StudentSchedulePreferenceService {

    private final StudentSchedulePreferenceRepository preferenceRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public void savePreferences(Long studentId, TimetableRequestDTO request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentSchedulePreference pref = preferenceRepository.findByStudentId(studentId)
                .orElse(
                        StudentSchedulePreference.builder()
                                .student(student)
                                .build()
                );

        pref.setAvoidEarlyMorning(Boolean.TRUE.equals(request.getAvoidEarlyMorning()));
        pref.setEarliestStartTime(request.getEarliestStartTime());
        pref.setMaxConsecutiveHoursPerDay(
                request.getMaxConsecutiveHours() != null ? request.getMaxConsecutiveHours() : 4
        );
        pref.setMaxGapMinutes(
                request.getMaxGapMinutes() != null ? request.getMaxGapMinutes() : 90
        );

        List<DayOfWeek> daysOff = new ArrayList<>();
        if (request.getPreferredDaysOff() != null) {
            for (String day : request.getPreferredDaysOff()) {
                daysOff.add(DayOfWeek.valueOf(day));
            }
        }

        pref.setPreferredDaysOff(daysOff);
        pref.setPreferDayOff(!daysOff.isEmpty());

        preferenceRepository.save(pref);
    }
}