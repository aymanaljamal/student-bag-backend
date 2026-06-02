package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import com.studentbag.backend.domain.enums.schedule.ScheduleSourceType;
import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.events.repository.EventRepository;
import com.studentbag.backend.notifications.dto.request.CreateNotificationRequest;
import com.studentbag.backend.notifications.service.NotificationService;
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
import com.studentbag.backend.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleManagementServiceImpl implements ScheduleManagementService {

    private final StudentScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final CourseSectionRepository courseSectionRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void activateSchedule(Long scheduleId, Long studentId) {
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);

        if (schedule.getStatus() == ScheduleStatus.DELETED) {
            throw new RuntimeException("Cannot activate a deleted schedule");
        }

        Long termId = schedule.getTerm() != null ? schedule.getTerm().getId() : null;

        if (termId == null) {
            throw new RuntimeException("Schedule term is required");
        }

        List<StudentSchedule> activeSchedules = scheduleRepository
                .findAllByStudentIdAndTermIdAndStatus(
                        studentId,
                        termId,
                        ScheduleStatus.ACTIVE
                );

        activeSchedules.stream()
                .filter(activeSchedule -> activeSchedule.getId() != null)
                .filter(activeSchedule -> !activeSchedule.getId().equals(schedule.getId()))
                .forEach(activeSchedule ->activeSchedule.setStatus(ScheduleStatus.ARCHIVED));

        schedule.setStatus(ScheduleStatus.ACTIVE);
        schedule.setActivatedAt(LocalDateTime.now());

        scheduleRepository.saveAll(activeSchedules);
        scheduleRepository.save(schedule);

        log.info(
                "Schedule {} activated for student {}.Old active schedules were archived.",
                scheduleId,
                studentId
        );
    }

    @Override
    @Transactional
    public void archiveSchedule(Long scheduleId, Long studentId) {
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);

        if (schedule.getStatus() == ScheduleStatus.DELETED) {
            throw new RuntimeException("Cannot archive a deleted schedule");
        }

        if (schedule.getStatus() == ScheduleStatus.ARCHIVED) {
            throw new RuntimeException("Schedule is already archived");
        }

        if (schedule.getStatus() == ScheduleStatus.ACTIVE) {
            throw new RuntimeException("Cannot archive active schedule. Activate another schedule first.");
        }

        schedule.setStatus(ScheduleStatus.ARCHIVED);
        scheduleRepository.save(schedule);

        log.info("Schedule {} archived by student {}", scheduleId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentScheduleResponseDTO> getStudentSchedules(Long studentId) {
        return scheduleRepository.findAllByStudentId(studentId)
                .stream()
                .map(scheduleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId, Long studentId) {
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);

        if (schedule.getStatus() == ScheduleStatus.ACTIVE) {
            throw new RuntimeException("Cannot delete active schedule. Activate another schedule first.");
        }

        if (schedule.getStatus() == ScheduleStatus.DELETED) {
            log.info("Schedule {} is already soft deleted for student {}", scheduleId, studentId);
            return;
        }

        schedule.setStatus(ScheduleStatus.DELETED);
        scheduleRepository.save(schedule);

        log.info("Schedule {} soft deleted by student {}", scheduleId, studentId);
    }

    @Override
    @Transactional
    public UpdateScheduleResponseDTO updateScheduleEntries(
            Long scheduleId,
            Long studentId,
            UpdateScheduleRequest request
    ) {
        StudentSchedule schedule = findAndValidate(scheduleId, studentId);

        if (schedule.getStatus() == ScheduleStatus.ARCHIVED
                || schedule.getStatus() == ScheduleStatus.DELETED) {
            throw new RuntimeException("Cannot edit an archived or deleted schedule");
        }

        List<UpdateScheduleEntryRequest> requestedEntries =
                request.getEntries() == null
                        ? Collections.emptyList()
                        : request.getEntries();

        List<ScheduleEntry> newEntries = requestedEntries
                .stream()
                .map(dto -> buildEntry(dto, schedule))
                .collect(Collectors.toList());

        addMissingHiddenCourseEntries(request, schedule, newEntries);

        List<ConflictDTO> conflicts = detectConflicts(newEntries);

        schedule.getEntries().clear();

        for (ScheduleEntry entry : newEntries) {
            schedule.addEntry(entry);
        }

        StudentSchedule savedSchedule = scheduleRepository.save(schedule);

        notifyScheduleOwner(
                savedSchedule,
                conflicts.isEmpty()
                        ? "Schedule updated"
                        : "Schedule updated with conflicts",
                conflicts.isEmpty()
                        ? "Your schedule was updated successfully."
                        : "Your schedule was updated, but some entries have time conflicts.",
                conflicts.isEmpty()
                        ? NotificationPriority.NORMAL
                        : NotificationPriority.HIGH
        );

        log.info(
                "Schedule {} updated with {} entries, {} conflicts",
                scheduleId,
                newEntries.size(),
                conflicts.size()
        );

        return UpdateScheduleResponseDTO.builder()
                .schedule(scheduleMapper.toViewerDTO(savedSchedule))
                .conflicts(conflicts)
                .hasConflicts(!conflicts.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveScheduleCourseDTO> getActiveScheduleCourses(Long studentId, Long termId) {
        return scheduleRepository
                .findByStudentIdAndTermIdAndStatus(
                        studentId,
                        termId,
                        ScheduleStatus.ACTIVE
                )
                .map(activeSchedule -> activeSchedule.getEntries()
                        .stream()
                        .filter(entry -> entry.getSourceType() == ScheduleSourceType.COURSE)
                        .filter(entry -> entry.getCourseSection() != null)
                        .filter(entry -> entry.getCourseSection().getCourse() != null)
                        .collect(Collectors.toMap(
                                entry -> entry.getCourseSection().getCourse().getId(),
                                this::mapEntryToActiveCourseDTO,
                                (first, second) -> first
                        ))
                        .values()
                        .stream()
                        .toList())
                .orElseGet(() -> {
                    log.warn(
                            "Active schedule not found for studentId={} and termId={}. Returning empty course list.",
                            studentId,
                            termId
                    );
                    return List.of();
                });
    }

    private StudentSchedule findAndValidate(Long scheduleId, Long studentId) {
        StudentSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (schedule.getStudent() == null || schedule.getStudent().getId() == null) {
            throw new RuntimeException("Schedule student is missing");
        }

        if (!schedule.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized");
        }

        return schedule;
    }

    private ScheduleEntry buildEntry(
            UpdateScheduleEntryRequest dto,
            StudentSchedule schedule
    ) {
        if (dto.getSourceType() == null) {
            throw new RuntimeException("sourceType is required");
        }

        boolean isAllDay = Boolean.TRUE.equals(dto.getIsAllDay());

        LocalDateTime start = dto.getStartDateTime();
        LocalDateTime end = dto.getEndDateTime();

        boolean isHiddenCourseEntry =
                dto.getSourceType() == ScheduleSourceType.COURSE && isAllDay;

        if (isHiddenCourseEntry) {
            if (dto.getCourseSectionId() == null) {
                throw new RuntimeException("courseSectionId is required for course entry");
            }

            if (start == null) {
                start = getHiddenEntryStartTime();
            }

            if (end == null || !end.isAfter(start)) {
                end = start.plusMinutes(1);
            }
        }

        if (start == null || end == null) {
            throw new RuntimeException("startDateTime and endDateTime are required");
        }

        if (!end.isAfter(start)) {
            throw new RuntimeException("endDateTime must be after startDateTime");
        }

        ScheduleEntry entry = ScheduleEntry.builder()
                .schedule(schedule)
                .student(schedule.getStudent())
                .title(resolveTitle(dto))
                .description(dto.getDescription())
                .location(dto.getLocation())
                .startDateTime(start)
                .endDateTime(end)
                .isAllDay(isAllDay)
                .sourceType(dto.getSourceType())
                .colorHex(dto.getColorHex())
                .isLocked(false)
                .build();

        if (dto.getSourceType() == ScheduleSourceType.COURSE) {
            if (dto.getCourseSectionId() == null) {
                throw new RuntimeException("courseSectionId is required for course entry");
            }

            CourseSection section = courseSectionRepository.findById(dto.getCourseSectionId())
                    .orElseThrow(() -> new RuntimeException(
                            "CourseSection not found: " + dto.getCourseSectionId()
                    ));

            entry.setCourseSection(section);
        }

        if (dto.getSourceType() == ScheduleSourceType.EVENT) {
            if (dto.getEventId() == null) {
                throw new RuntimeException("eventId is required for event entry");
            }

            entry.setEvent(
                    eventRepository.findById(dto.getEventId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Event not found: " + dto.getEventId()
                            ))
            );
        }

        return entry;
    }

    private void addMissingHiddenCourseEntries(
            UpdateScheduleRequest request,
            StudentSchedule schedule,
            List<ScheduleEntry> entries
    ) {
        if (request.getSelectedCourseSectionIds() == null
                || request.getSelectedCourseSectionIds().isEmpty()) {
            return;
        }

        Set<Long> existingSectionIds = entries.stream()
                .filter(entry -> entry.getSourceType() == ScheduleSourceType.COURSE)
                .filter(entry -> entry.getCourseSection() != null)
                .map(entry -> entry.getCourseSection().getId())
                .collect(Collectors.toSet());

        Set<Long> requestedSectionIds = new HashSet<>(request.getSelectedCourseSectionIds());

        for (Long sectionId : requestedSectionIds) {
            if (sectionId == null || existingSectionIds.contains(sectionId)) {
                continue;
            }

            CourseSection section = courseSectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException(
                            "CourseSection not found: " + sectionId
                    ));

            LocalDateTime start = getHiddenEntryStartTime();

            ScheduleEntry hiddenEntry = ScheduleEntry.builder()
                    .schedule(schedule)
                    .student(schedule.getStudent())
                    .sourceType(ScheduleSourceType.COURSE)
                    .courseSection(section)
                    .title(resolveCourseSectionTitle(section))
                    .description(null)
                    .location(null)
                    .startDateTime(start)
                    .endDateTime(start.plusMinutes(1))
                    .isAllDay(true)
                    .colorHex(null)
                    .isLocked(false)
                    .build();

            entries.add(hiddenEntry);
            existingSectionIds.add(sectionId);
        }
    }

    private void notifyScheduleOwner(
            StudentSchedule schedule,
            String title,
            String body,
            NotificationPriority priority
    ) {
        if (schedule == null
                || schedule.getStudent() == null
                || schedule.getStudent().getUser() == null
                || schedule.getStudent().getUser().getId() == null) {
            return;
        }

        User user = schedule.getStudent().getUser();
        UUID userId = user.getId();

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle(title);
        request.setBody(body);
        request.setType(NotificationType.SYSTEM);
        request.setPriority(priority);
        request.setChannel(NotificationChannel.BOTH);
        request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
        request.setTargetValue("/student/schedule");
        request.setBroadcastToAll(false);
        request.setRecipientUserIds(List.of(userId));

        notificationService.createAndSend(request);
    }

    private String resolveTitle(UpdateScheduleEntryRequest dto) {
        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            return dto.getTitle().trim();
        }

        if (dto.getSourceType() == ScheduleSourceType.COURSE) {
            return "Course";
        }

        if (dto.getSourceType() == ScheduleSourceType.EVENT) {
            return "Event";
        }

        return "Schedule Entry";
    }

    private String resolveCourseSectionTitle(CourseSection section) {
        if (section.getCourse() == null) {
            return "Course";
        }

        Course course = section.getCourse();

        if (course.getCode() != null && !course.getCode().trim().isEmpty()) {
            return course.getCode().trim();
        }

        if (course.getNameEnglish() != null && !course.getNameEnglish().trim().isEmpty()) {
            return course.getNameEnglish().trim();
        }

        if (course.getNameArabic() != null && !course.getNameArabic().trim().isEmpty()) {
            return course.getNameArabic().trim();
        }

        return "Course";
    }

    private LocalDateTime getHiddenEntryStartTime() {
        return LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private List<ConflictDTO> detectConflicts(List<ScheduleEntry> entries) {
        List<ConflictDTO> conflicts = new ArrayList<>();

        List<ScheduleEntry> timedEntries = entries.stream()
                .filter(entry -> !Boolean.TRUE.equals(entry.getIsAllDay()))
                .toList();

        for (int i = 0; i < timedEntries.size(); i++) {
            for (int j = i + 1; j < timedEntries.size(); j++) {
                ScheduleEntry a = timedEntries.get(i);
                ScheduleEntry b = timedEntries.get(j);

                if (a.conflictsWith(b)) {
                    conflicts.add(ConflictDTO.builder()
                            .entryATitle(a.getTitle())
                            .entryBTitle(b.getTitle())
                            .conflictStart(
                                    a.getStartDateTime().isAfter(b.getStartDateTime())
                                            ? a.getStartDateTime()
                                            : b.getStartDateTime()
                            )
                            .conflictEnd(
                                    a.getEndDateTime().isBefore(b.getEndDateTime())
                                            ? a.getEndDateTime()
                                            : b.getEndDateTime()
                            )
                            .build());
                }
            }
        }

        return conflicts;
    }

    private ActiveScheduleCourseDTO mapEntryToActiveCourseDTO(ScheduleEntry entry) {
        Course course = entry.getCourseSection().getCourse();

        Long instructorId = null;
        String instructorNameArabic = null;
        String instructorNameEnglish = null;

        if (entry.getCourseSection().getInstructor() != null) {
            instructorId = entry.getCourseSection().getInstructor().getId();
            instructorNameArabic = entry.getCourseSection().getInstructor().getFullNameArabic();
            instructorNameEnglish = entry.getCourseSection().getInstructor().getFullNameEnglish();
        }

        return ActiveScheduleCourseDTO.builder()
                .id(course.getId())
                .externalId(course.getExternalId())
                .code(course.getCode())
                .nameArabic(course.getNameArabic())
                .nameEnglish(course.getNameEnglish())
                .description(course.getDescription())
                .creditHours(course.getCreditHours())
                .courseSectionId(entry.getCourseSection().getId())
                .instructorId(instructorId)
                .instructorNameArabic(instructorNameArabic)
                .instructorNameEnglish(instructorNameEnglish)
                .build();
    }
}