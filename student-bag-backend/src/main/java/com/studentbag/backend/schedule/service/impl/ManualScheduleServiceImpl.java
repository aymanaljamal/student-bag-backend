package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.domain.enums.schedule.ScheduleSourceType;
import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.schedule.dto.request.CreateManualScheduleRequest;
import com.studentbag.backend.schedule.dto.request.ManualScheduleEntryRequest;
import com.studentbag.backend.schedule.dto.request.UpdateManualScheduleNameRequest;
import com.studentbag.backend.schedule.dto.response.ManualCourseOptionDto;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.mapper.ScheduleMapper;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.schedule.service.ManualScheduleService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualScheduleServiceImpl implements ManualScheduleService {

    private final StudentScheduleRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TermRepository termRepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ManualCourseOptionDto> getCourseOptionsForCurrentStudent() {
        return courseRepository.findAllForManualSchedulePicker()
                .stream()
                .map(course -> new ManualCourseOptionDto(
                        course.getId(),
                        course.getCode(),
                        course.getNameArabic(),
                        course.getNameEnglish(),
                        course.getCreditHours()
                ))
                .toList();
    }

    @Override
    @Transactional
    public StudentScheduleResponseDTO createManualSchedule(
            CreateManualScheduleRequest request
    ) {
        Student student = getCurrentStudent();
        Term term = getCurrentTerm();

        StudentSchedule schedule = StudentSchedule.builder()
                .student(student)
                .term(term)
                .name(cleanRequired(request.name(), "Schedule name is required"))
                .status(ScheduleStatus.DRAFT)
                .build();

        for (ManualScheduleEntryRequest entryRequest : request.entries()) {
            ScheduleEntry entry = buildManualEntry(schedule, entryRequest);
            schedule.addEntry(entry);
        }

        validateNoInternalConflicts(schedule.getEntries());

        StudentSchedule savedSchedule = scheduleRepository.save(schedule);

        log.info(
                "Manual schedule {} created for student {} with {} entries",
                savedSchedule.getId(),
                student.getId(),
                savedSchedule.getEntries().size()
        );

        return scheduleMapper.toResponseDTO(savedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentScheduleResponseDTO getManualSchedule(Long scheduleId) {
        StudentSchedule schedule = findScheduleForCurrentStudent(scheduleId);

        return scheduleMapper.toResponseDTO(schedule);
    }

    @Override
    @Transactional
    public StudentScheduleResponseDTO updateScheduleName(
            Long scheduleId,
            UpdateManualScheduleNameRequest request
    ) {
        StudentSchedule schedule = findScheduleForCurrentStudent(scheduleId);
        validateEditable(schedule);

        schedule.setName(cleanRequired(request.name(), "Schedule name is required"));

        StudentSchedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Manual schedule {} name updated", scheduleId);

        return scheduleMapper.toResponseDTO(savedSchedule);
    }

    @Override
    @Transactional
    public StudentScheduleResponseDTO addEntry(
            Long scheduleId,
            ManualScheduleEntryRequest request
    ) {
        StudentSchedule schedule = findScheduleForCurrentStudent(scheduleId);
        validateEditable(schedule);

        ScheduleEntry entry = buildManualEntry(schedule, request);
        schedule.addEntry(entry);

        validateNoInternalConflicts(schedule.getEntries());

        StudentSchedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Manual entry added to schedule {}", scheduleId);

        return scheduleMapper.toResponseDTO(savedSchedule);
    }

    @Override
    @Transactional
    public StudentScheduleResponseDTO updateEntry(
            Long scheduleId,
            Long entryId,
            ManualScheduleEntryRequest request
    ) {
        StudentSchedule schedule = findScheduleForCurrentStudent(scheduleId);
        validateEditable(schedule);

        ScheduleEntry entry = schedule.getEntries()
                .stream()
                .filter(item -> item.getId() != null && item.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Schedule entry not found"));

        if (entry.getSourceType() != ScheduleSourceType.MANUAL) {
            throw new RuntimeException("Only manual schedule entries can be edited here");
        }

        Course course = findCourse(request.courseId());

        LocalDateTime startDateTime = toDateTime(
                request.dayOfWeek(),
                request.startTime()
        );

        LocalDateTime endDateTime = toDateTime(
                request.dayOfWeek(),
                request.endTime()
        );

        validateTimeRange(startDateTime, endDateTime);

        entry.setManualCourse(course);
        entry.setManualSectionNumber(cleanOptional(request.sectionNumber()));
        entry.setTitle(resolveManualTitle(course, request.sectionNumber()));
        entry.setDescription(cleanOptional(request.note()));
        entry.setBuilding(cleanOptional(request.building()));
        entry.setRoom(cleanOptional(request.room()));
        entry.setLocation(resolveLocation(request.building(), request.room()));
        entry.setStartDateTime(startDateTime);
        entry.setEndDateTime(endDateTime);
        entry.setIsAllDay(false);
        entry.setIsLocked(false);

        validateNoInternalConflicts(schedule.getEntries());

        StudentSchedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Manual entry {} updated in schedule {}", entryId, scheduleId);

        return scheduleMapper.toResponseDTO(savedSchedule);
    }

    @Override
    @Transactional
    public StudentScheduleResponseDTO deleteEntry(
            Long scheduleId,
            Long entryId
    ) {
        StudentSchedule schedule = findScheduleForCurrentStudent(scheduleId);
        validateEditable(schedule);

        ScheduleEntry entry = schedule.getEntries()
                .stream()
                .filter(item -> item.getId() != null && item.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Schedule entry not found"));

        if (entry.getSourceType() != ScheduleSourceType.MANUAL) {
            throw new RuntimeException("Only manual schedule entries can be deleted here");
        }

        schedule.getEntries().remove(entry);

        StudentSchedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Manual entry {} deleted from schedule {}", entryId, scheduleId);

        return scheduleMapper.toResponseDTO(savedSchedule);
    }

    private ScheduleEntry buildManualEntry(
            StudentSchedule schedule,
            ManualScheduleEntryRequest request
    ) {
        Course course = findCourse(request.courseId());

        LocalDateTime startDateTime = toDateTime(
                request.dayOfWeek(),
                request.startTime()
        );

        LocalDateTime endDateTime = toDateTime(
                request.dayOfWeek(),
                request.endTime()
        );

        validateTimeRange(startDateTime, endDateTime);

        return ScheduleEntry.builder()
                .schedule(schedule)
                .student(schedule.getStudent())
                .sourceType(ScheduleSourceType.MANUAL)
                .manualCourse(course)
                .manualSectionNumber(cleanOptional(request.sectionNumber()))
                .title(resolveManualTitle(course, request.sectionNumber()))
                .description(cleanOptional(request.note()))
                .building(cleanOptional(request.building()))
                .room(cleanOptional(request.room()))
                .location(resolveLocation(request.building(), request.room()))
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .isAllDay(false)
                .isLocked(false)
                .build();
    }

    private StudentSchedule findScheduleForCurrentStudent(Long scheduleId) {
        Student student = getCurrentStudent();

        StudentSchedule schedule = scheduleRepository.findByIdWithEntries(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (schedule.getStudent() == null || schedule.getStudent().getId() == null) {
            throw new RuntimeException("Schedule student is missing");
        }

        if (!schedule.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return schedule;
    }

    private Student getCurrentStudent() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = authentication.getName();

        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
    }

    private Term getCurrentTerm() {
        return termRepository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("Current term not found"));
    }

    private Course findCourse(Long courseId) {
        if (courseId == null) {
            throw new RuntimeException("Course is required");
        }

        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    }

    private void validateEditable(StudentSchedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.ARCHIVED
                || schedule.getStatus() == ScheduleStatus.DELETED) {
            throw new RuntimeException("Cannot edit an archived or deleted schedule");
        }
    }

    private LocalDateTime toDateTime(
            DayOfWeek dayOfWeek,
            LocalTime time
    ) {
        if (dayOfWeek == null) {
            throw new RuntimeException("Day is required");
        }

        if (time == null) {
            throw new RuntimeException("Time is required");
        }

        LocalDate weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate date = weekStart.plusDays(dayOfWeek.getValue() - 1L);

        return LocalDateTime.of(date, time);
    }

    private void validateTimeRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        if (startDateTime == null || endDateTime == null) {
            throw new RuntimeException("Start and end time are required");
        }

        if (!endDateTime.isAfter(startDateTime)) {
            throw new RuntimeException("End time must be after start time");
        }
    }

    private void validateNoInternalConflicts(List<ScheduleEntry> entries) {
        List<ScheduleEntry> timedEntries = entries.stream()
                .filter(entry -> !Boolean.TRUE.equals(entry.getIsAllDay()))
                .toList();

        for (int i = 0; i < timedEntries.size(); i++) {
            for (int j = i + 1; j < timedEntries.size(); j++) {
                ScheduleEntry first = timedEntries.get(i);
                ScheduleEntry second = timedEntries.get(j);

                boolean sameEntry = first.getId() != null
                        && second.getId() != null
                        && first.getId().equals(second.getId());

                if (!sameEntry && first.conflictsWith(second)) {
                    throw new RuntimeException(
                            "Schedule conflict between "
                                    + first.getTitle()
                                    + " and "
                                    + second.getTitle()
                    );
                }
            }
        }
    }

    private String resolveManualTitle(
            Course course,
            String sectionNumber
    ) {
        String courseTitle;

        if (course.getCode() != null && !course.getCode().trim().isEmpty()) {
            courseTitle = course.getCode().trim();
        } else if (course.getNameEnglish() != null && !course.getNameEnglish().trim().isEmpty()) {
            courseTitle = course.getNameEnglish().trim();
        } else if (course.getNameArabic() != null && !course.getNameArabic().trim().isEmpty()) {
            courseTitle = course.getNameArabic().trim();
        } else {
            courseTitle = "Course";
        }

        String cleanSection = cleanOptional(sectionNumber);

        if (cleanSection == null) {
            return courseTitle;
        }

        return courseTitle + " - Section " + cleanSection;
    }

    private String resolveLocation(
            String building,
            String room
    ) {
        String cleanBuilding = cleanOptional(building);
        String cleanRoom = cleanOptional(room);

        if (cleanBuilding == null && cleanRoom == null) {
            return null;
        }

        if (cleanBuilding == null) {
            return cleanRoom;
        }

        if (cleanRoom == null) {
            return cleanBuilding;
        }

        return cleanBuilding + " " + cleanRoom;
    }

    private String cleanRequired(
            String value,
            String message
    ) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(message);
        }

        return value.trim();
    }

    private String cleanOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}