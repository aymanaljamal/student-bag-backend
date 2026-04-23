package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.domain.enums.schedule.ScheduleSourceType;
import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.schedule.dto.ConflictDTO;
import com.studentbag.backend.schedule.dto.request.UpdateScheduleEntryRequest;
import com.studentbag.backend.schedule.dto.request.UpdateScheduleRequest;
import com.studentbag.backend.schedule.dto.response.ActiveScheduleCourseDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.dto.response.UpdateScheduleResponseDTO;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.mapper.ScheduleMapper;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.schedule.service.ScheduleManagementService;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleManagementServiceImpl implements ScheduleManagementService {

    private final StudentScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final CourseSectionRepository courseSectionRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void activateSchedule(Long scheduleId, Long studentId) {
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);

        List<StudentSchedule> active = scheduleRepository
                .findAllByStudentIdAndTermIdAndStatus(
                        studentId,
                        schedule.getTerm().getId(),
                        ScheduleStatus.ACTIVE
                );
        active.forEach(s -> s.setStatus(ScheduleStatus.ARCHIVED));
        schedule.setStatus(ScheduleStatus.ACTIVE);

        scheduleRepository.saveAll(active);
        scheduleRepository.save(schedule);
        log.info("Schedule {} activated for student {}", scheduleId, studentId);
    }

    @Override
    @Transactional
    public void archiveSchedule(Long scheduleId, Long studentId) {
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);

        if (schedule.getStatus() == ScheduleStatus.ARCHIVED) {
            throw new RuntimeException("Schedule is already archived");
        }

        schedule.setStatus(ScheduleStatus.ARCHIVED);
        scheduleRepository.save(schedule);
        log.info("Schedule {} archived by student {}", scheduleId, studentId);
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
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);
        scheduleRepository.delete(schedule);
        log.info("Schedule {} deleted by student {}", scheduleId, studentId);
    }

    @Override
    @Transactional
    public UpdateScheduleResponseDTO updateScheduleEntries(
            Long scheduleId,
            Long studentId,
            UpdateScheduleRequest request
    ) {
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);

        if (schedule.getStatus() == ScheduleStatus.ARCHIVED) {
            throw new RuntimeException("Cannot edit an archived schedule");
        }

        // بناء الـ entries الجديدة
        List<ScheduleEntry> newEntries = request.getEntries().stream()
                .map(dto -> buildEntry(dto, schedule))
                .collect(Collectors.toList());

        // كشف الـ conflicts
        List<ConflictDTO> conflicts = detectConflicts(newEntries);

        // مسح القديم وحفظ الجديد
        schedule.getEntries().clear();
        newEntries.forEach(schedule::addEntry);
        scheduleRepository.save(schedule);

        log.info("Schedule {} updated with {} entries, {} conflicts",
                scheduleId, newEntries.size(), conflicts.size());

        return UpdateScheduleResponseDTO.builder()
                .schedule(scheduleMapper.toViewerDTO(schedule))
                .conflicts(conflicts)
                .hasConflicts(!conflicts.isEmpty())
                .build();
    }

    // ─── Helpers ─────────────────────────────────

    private StudentSchedule findAndValidate(Long scheduleId, Long studentId) {
        StudentSchedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        if (!s.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized");
        }
        return s;
    }

    private ScheduleEntry buildEntry(UpdateScheduleEntryRequest dto,
                                     StudentSchedule schedule) {
        ScheduleEntry entry = ScheduleEntry.builder()
                .schedule(schedule)
                .student(schedule.getStudent())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .isAllDay(dto.getIsAllDay() != null ? dto.getIsAllDay() : false)
                .sourceType(dto.getSourceType())
                .colorHex(dto.getColorHex())
                .isLocked(false)
                .build();

        if (dto.getSourceType() == ScheduleSourceType.COURSE
                && dto.getCourseSectionId() != null) {
            entry.setCourseSection(
                    courseSectionRepository.findById(dto.getCourseSectionId())
                            .orElseThrow(() -> new RuntimeException(
                                    "CourseSection not found: " + dto.getCourseSectionId()))
            );
        }

        if (dto.getSourceType() == ScheduleSourceType.EVENT
                && dto.getEventId() != null) {
            entry.setEvent(
                    eventRepository.findById(dto.getEventId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Event not found: " + dto.getEventId()))
            );
        }

        return entry;
    }

    private List<ConflictDTO> detectConflicts(List<ScheduleEntry> entries) {
        List<ConflictDTO> conflicts = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                ScheduleEntry a = entries.get(i);
                ScheduleEntry b = entries.get(j);
                if (a.conflictsWith(b)) {
                    conflicts.add(ConflictDTO.builder()
                            .entryATitle(a.getTitle())
                            .entryBTitle(b.getTitle())
                            .conflictStart(
                                    a.getStartDateTime().isAfter(b.getStartDateTime())
                                            ? a.getStartDateTime() : b.getStartDateTime()
                            )
                            .conflictEnd(
                                    a.getEndDateTime().isBefore(b.getEndDateTime())
                                            ? a.getEndDateTime() : b.getEndDateTime()
                            )
                            .build());
                }
            }
        }
        return conflicts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveScheduleCourseDTO> getActiveScheduleCourses(Long studentId, Long termId) {
        StudentSchedule activeSchedule = scheduleRepository
                .findByStudentIdAndTermIdAndStatus(studentId, termId, ScheduleStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active schedule not found for this term"));

        return activeSchedule.getEntries().stream()
                .filter(entry -> entry.getSourceType() == ScheduleSourceType.COURSE)
                .filter(entry -> entry.getCourseSection() != null)
                .filter(entry -> entry.getCourseSection().getCourse() != null)
                .map(entry -> entry.getCourseSection().getCourse())
                .distinct()
                .map(this::mapCourseToDTO)
                .toList();
    }

    private ActiveScheduleCourseDTO mapCourseToDTO(Course course) {
        return ActiveScheduleCourseDTO.builder()
                .id(course.getId())
                .externalId(course.getExternalId())
                .code(course.getCode())
                .nameArabic(course.getNameArabic())
                .nameEnglish(course.getNameEnglish())
                .description(course.getDescription())
                .creditHours(course.getCreditHours())
                .build();
    }
}