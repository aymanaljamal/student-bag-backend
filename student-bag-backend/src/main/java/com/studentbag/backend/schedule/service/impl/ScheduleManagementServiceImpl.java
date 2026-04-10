package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.domain.enums.ScheduleStatus;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.mapper.ScheduleMapper;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.schedule.service.ScheduleManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleManagementServiceImpl implements ScheduleManagementService {

    private final StudentScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public void activateSchedule(Long scheduleId, Long studentId) {
        StudentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized: Ownership mismatch");
        }

        List<StudentSchedule> activeSchedules = scheduleRepository
                .findAllByStudentIdAndTermIdAndStatus(
                        studentId,
                        schedule.getTerm().getId(),
                        ScheduleStatus.ACTIVE
                );

        activeSchedules.forEach(s -> s.setStatus(ScheduleStatus.ARCHIVED));

        schedule.setStatus(ScheduleStatus.ACTIVE);

        scheduleRepository.saveAll(activeSchedules);
        scheduleRepository.save(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentScheduleResponseDTO> getStudentSchedules(Long studentId) {
        return scheduleRepository.findAllByStudentId(studentId).stream()
                .map(scheduleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId, Long studentId) {
        StudentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized delete attempt");
        }

        scheduleRepository.delete(schedule);
        log.info("Schedule {} deleted by student {}", scheduleId, studentId);
    }
}