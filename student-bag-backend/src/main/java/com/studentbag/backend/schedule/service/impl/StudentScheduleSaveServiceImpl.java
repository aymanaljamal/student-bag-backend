package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.domain.enums.schedule.ScheduleSourceType;
import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.schedule.dto.ClassSessionDTO;
import com.studentbag.backend.schedule.dto.CourseSectionDTO;
import com.studentbag.backend.schedule.dto.request.SaveScheduleRequestDTO;
import com.studentbag.backend.schedule.dto.response.ScheduleViewerEntryResponseDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleViewerResponseDTO;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.schedule.service.StudentScheduleSaveService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentScheduleSaveServiceImpl implements StudentScheduleSaveService {

    private final StudentScheduleRepository studentScheduleRepository;
    private final StudentRepository studentRepository;
    private final TermRepository termRepository;
    private final CourseSectionRepository courseSectionRepository;

    @Override
    @Transactional
    public StudentScheduleResponseDTO saveSelectedSchedule(SaveScheduleRequestDTO request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Term term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new RuntimeException("Term not found"));

        if (request.getOption() == null) {
            throw new RuntimeException("Schedule option is required");
        }

        StudentSchedule schedule = StudentSchedule.builder()
                .student(student)
                .term(term)
                .status(ScheduleStatus.DRAFT)
                .build();

        addOptionSessionEntries(request, schedule, student);

        addMissingHiddenCourseEntries(request, schedule, student);

        StudentSchedule saved = studentScheduleRepository.save(schedule);

        return StudentScheduleResponseDTO.builder()
                .id(saved.getId())
                .termId(saved.getTerm() != null ? saved.getTerm().getId() : null)
                .termName(saved.getTerm() != null ? saved.getTerm().getName() : null)
                .status(saved.getStatus())
                .build();
    }

    @Override
    @Transactional
    public StudentScheduleViewerResponseDTO activateSchedule(Long scheduleId) {
        StudentSchedule target = studentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Long studentId = target.getStudent().getId();
        Long termId = target.getTerm().getId();

        List<StudentSchedule> schedules = studentScheduleRepository
                .findByStudent_IdAndTerm_Id(studentId, termId);

        for (StudentSchedule schedule : schedules) {
            if (schedule.getId().equals(target.getId())) {
                schedule.setStatus(ScheduleStatus.ACTIVE);
                schedule.setActivatedAt(LocalDateTime.now());
            } else if (schedule.getStatus() == ScheduleStatus.ACTIVE) {
                schedule.setStatus(ScheduleStatus.DRAFT);
            }
        }

        studentScheduleRepository.saveAll(schedules);

        return mapToViewerResponse(target);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentScheduleViewerResponseDTO getActiveSchedule(Long studentId) {
        StudentSchedule schedule = studentScheduleRepository
                .findFirstByStudent_IdAndStatusOrderByActivatedAtDescCreatedAtDesc(
                        studentId,
                        ScheduleStatus.ACTIVE
                )
                .orElseThrow(() -> new RuntimeException("No active schedule found"));

        return mapToViewerResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentScheduleViewerResponseDTO> getStudentSchedules(Long studentId) {
        return studentScheduleRepository.findByStudent_IdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::mapToViewerResponse)
                .toList();
    }

    private void addOptionSessionEntries(
            SaveScheduleRequestDTO request,
            StudentSchedule schedule,
            Student student
    ) {
        if (request.getOption().getSections() == null
                || request.getOption().getSections().isEmpty()) {
            return;
        }

        for (CourseSectionDTO section : request.getOption().getSections()) {
            if (section.getSessions() == null || section.getSessions().isEmpty()) {
                continue;
            }

            CourseSection courseSection = null;

            if (section.getSectionId() != null) {
                courseSection = courseSectionRepository.findById(section.getSectionId())
                        .orElse(null);
            }

            for (ClassSessionDTO session : section.getSessions()) {
                if (session.getDay() == null
                        || session.getStartTime() == null
                        || session.getEndTime() == null) {
                    continue;
                }

                LocalDate sessionDate = resolveDateForDay(session.getDay());

                LocalDateTime startDateTime = LocalDateTime.of(
                        sessionDate,
                        session.getStartTime()
                );

                LocalDateTime endDateTime = LocalDateTime.of(
                        sessionDate,
                        session.getEndTime()
                );

                if (!endDateTime.isAfter(startDateTime)) {
                    continue;
                }

                ScheduleEntry entry = ScheduleEntry.builder()
                        .student(student)
                        .sourceType(ScheduleSourceType.COURSE)
                        .courseSection(courseSection)
                        .title(buildTitle(section))
                        .description(buildDescription(section))
                        .location(buildLocation(session))
                        .startDateTime(startDateTime)
                        .endDateTime(endDateTime)
                        .isAllDay(false)
                        .isLocked(false)
                        .build();

                schedule.addEntry(entry);
            }
        }
    }

    private void addMissingHiddenCourseEntries(
            SaveScheduleRequestDTO request,
            StudentSchedule schedule,
            Student student
    ) {
        if (request.getSelectedCourseSectionIds() == null
                || request.getSelectedCourseSectionIds().isEmpty()) {
            return;
        }

        Set<Long> existingSectionIds = schedule.getEntries()
                .stream()
                .filter(entry -> entry.getSourceType() == ScheduleSourceType.COURSE)
                .filter(entry -> entry.getCourseSection() != null)
                .map(entry -> entry.getCourseSection().getId())
                .collect(Collectors.toSet());

        Set<Long> requestedSectionIds = new HashSet<>(request.getSelectedCourseSectionIds());

        for (Long sectionId : requestedSectionIds) {
            if (sectionId == null || existingSectionIds.contains(sectionId)) {
                continue;
            }

            CourseSection courseSection = courseSectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException(
                            "CourseSection not found: " + sectionId
                    ));

            LocalDateTime start = hiddenEntryStartTime();

            ScheduleEntry hiddenEntry = ScheduleEntry.builder()
                    .student(student)
                    .sourceType(ScheduleSourceType.COURSE)
                    .courseSection(courseSection)
                    .title(buildTitle(courseSection))
                    .description(buildDescription(courseSection))
                    .location(null)
                    .startDateTime(start)
                    .endDateTime(start.plusMinutes(1))
                    .isAllDay(true)
                    .isLocked(false)
                    .build();

            schedule.addEntry(hiddenEntry);
            existingSectionIds.add(sectionId);
        }
    }

    private StudentScheduleViewerResponseDTO mapToViewerResponse(StudentSchedule schedule) {
        return StudentScheduleViewerResponseDTO.builder()
                .id(schedule.getId())
                .termId(schedule.getTerm().getId())
                .termName(schedule.getTerm().getName())
                .status(schedule.getStatus())
                .entries(schedule.getEntries().stream().map(entry -> {
                    Integer instructorId = null;
                    String instructorName = null;

                    if (entry.getCourseSection() != null
                            && entry.getCourseSection().getInstructor() != null) {
                        instructorId = entry.getCourseSection()
                                .getInstructor()
                                .getId()
                                .intValue();

                        instructorName = entry.getCourseSection()
                                .getInstructor()
                                .getFullNameEnglish();
                    }

                    return ScheduleViewerEntryResponseDTO.builder()
                            .id(entry.getId())
                            .title(entry.getTitle())
                            .description(entry.getDescription())
                            .location(entry.getLocation())
                            .startDateTime(entry.getStartDateTime())
                            .endDateTime(entry.getEndDateTime())
                            .isAllDay(Boolean.TRUE.equals(entry.getIsAllDay()))
                            .sourceType(entry.getSourceType().name())
                            .isLocked(Boolean.TRUE.equals(entry.getIsLocked()))
                            .courseSectionId(
                                    entry.getCourseSection() != null
                                            ? entry.getCourseSection().getId().intValue()
                                            : null
                            )
                            .eventId(
                                    entry.getEvent() != null
                                            ? entry.getEvent().getId().intValue()
                                            : null
                            )
                            .instructorId(instructorId)
                            .instructorName(instructorName)
                            .build();
                }).toList())
                .build();
    }

    private LocalDate resolveDateForDay(DayOfWeek day) {
        LocalDate now = LocalDate.now();
        return now.with(TemporalAdjusters.nextOrSame(day));
    }

    private LocalDateTime hiddenEntryStartTime() {
        return LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private String buildTitle(CourseSectionDTO section) {
        if (section.getCourseCode() != null && !section.getCourseCode().isBlank()) {
            return section.getCourseCode();
        }

        if (section.getCourseName() != null && !section.getCourseName().isBlank()) {
            return section.getCourseName();
        }

        return "Course";
    }

    private String buildDescription(CourseSectionDTO section) {
        StringBuilder sb = new StringBuilder();

        if (section.getCourseName() != null && !section.getCourseName().isBlank()) {
            sb.append(section.getCourseName());
        }

        if (section.getSectionNumber() != null && !section.getSectionNumber().isBlank()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append("Section ").append(section.getSectionNumber());
        }

        if (section.getInstructorName() != null && !section.getInstructorName().isBlank()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append(section.getInstructorName());
        }

        return sb.toString();
    }

    private String buildLocation(ClassSessionDTO session) {
        StringBuilder sb = new StringBuilder();

        if (session.getBuilding() != null && !session.getBuilding().isBlank()) {
            sb.append(session.getBuilding());
        }

        if (session.getRoom() != null && !session.getRoom().isBlank()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append(session.getRoom());
        }

        return sb.toString();
    }

    private String buildTitle(CourseSection section) {
        if (section.getCourse() == null) {
            return "Course";
        }

        Course course = section.getCourse();

        if (course.getCode() != null && !course.getCode().isBlank()) {
            return course.getCode();
        }

        if (course.getNameEnglish() != null && !course.getNameEnglish().isBlank()) {
            return course.getNameEnglish();
        }

        if (course.getNameArabic() != null && !course.getNameArabic().isBlank()) {
            return course.getNameArabic();
        }

        return "Course";
    }

    private String buildDescription(CourseSection section) {
        StringBuilder sb = new StringBuilder();

        if (section.getCourse() != null) {
            Course course = section.getCourse();

            if (course.getNameEnglish() != null && !course.getNameEnglish().isBlank()) {
                sb.append(course.getNameEnglish());
            } else if (course.getNameArabic() != null && !course.getNameArabic().isBlank()) {
                sb.append(course.getNameArabic());
            }
        }

        if (section.getSectionNumber() != null && !section.getSectionNumber().isBlank()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append("Section ").append(section.getSectionNumber());
        }

        if (section.getInstructor() != null
                && section.getInstructor().getFullNameEnglish() != null
                && !section.getInstructor().getFullNameEnglish().isBlank()) {
            if (!sb.isEmpty()) sb.append(" • ");
            sb.append(section.getInstructor().getFullNameEnglish());
        }

        return sb.toString();
    }
}