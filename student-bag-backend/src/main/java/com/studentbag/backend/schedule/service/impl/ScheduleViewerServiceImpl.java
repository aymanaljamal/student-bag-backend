package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.schedule.dto.response.StudentScheduleViewerResponseDTO;
import com.studentbag.backend.schedule.mapper.ScheduleMapper;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.schedule.service.ScheduleViewerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleViewerServiceImpl implements ScheduleViewerService {

    private final StudentScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StudentScheduleViewerResponseDTO> getStudentSchedulesViewer(Long studentId) {
        var schedules = scheduleRepository.findAllByStudentId(studentId);

        for (var schedule : schedules) {
            if (schedule.getEntries() == null) {
                log.info("VIEWER SCHEDULE id={} has null entries", schedule.getId());
                continue;
            }

            for (var entry : schedule.getEntries()) {
                log.info(
                        "ENTITY ENTRY -> scheduleId={}, entryId={}, title={}, colorHex={}",
                        schedule.getId(),
                        entry.getId(),
                        entry.getTitle(),
                        entry.getColorHex()
                );

                var dto = scheduleMapper.toEntryViewerDTO(entry);

                log.info(
                        "DTO ENTRY -> scheduleId={}, entryId={}, title={}, colorHex={}",
                        schedule.getId(),
                        dto != null ? dto.getId() : null,
                        dto != null ? dto.getTitle() : null,
                        dto != null ? dto.getColorHex() : null
                );
            }
        }

        var result = schedules.stream()
                .map(scheduleMapper::toViewerDTO)
                .toList();

        for (var scheduleDto : result) {
            if (scheduleDto.getEntries() == null) {
                log.info("VIEWER RESPONSE scheduleId={} has null dto entries", scheduleDto.getId());
                continue;
            }

            for (var entryDto : scheduleDto.getEntries()) {
                log.info(
                        "FINAL RESPONSE ENTRY -> scheduleId={}, entryId={}, title={}, colorHex={}",
                        scheduleDto.getId(),
                        entryDto.getId(),
                        entryDto.getTitle(),
                        entryDto.getColorHex()
                );
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentScheduleViewerResponseDTO getScheduleViewer(Long scheduleId, Long studentId) {
        var schedule = scheduleRepository.findByIdWithEntries(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized");
        }

        return scheduleMapper.toViewerDTO(schedule);
    }
}