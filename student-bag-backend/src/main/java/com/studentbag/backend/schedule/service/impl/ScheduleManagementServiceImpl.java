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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleManagementServiceImpl implements ScheduleManagementService {

    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final StudentScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final CourseSectionRepository courseSectionRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void activateSchedule(Long scheduleId, Long studentId) {
        StudentSchedule targetSchedule = findAndValidate(scheduleId, studentId);

        if (targetSchedule.getStatus() == ScheduleStatus.DELETED) {
            throw new RuntimeException("Cannot activate a deleted schedule");
        }

        Long termId = targetSchedule.getTerm() != null ? targetSchedule.getTerm().getId() : null;

        if (termId == null) {
            throw new RuntimeException("Schedule term is required");
        }

        List<StudentSchedule> activeSchedules = scheduleRepository
                .findAllByStudentIdAndTermIdAndStatus(
                        studentId,
                        termId,
                        ScheduleStatus.ACTIVE
                );

        StudentSchedule previousActiveSchedule = activeSchedules.stream()
                .filter(activeSchedule -> activeSchedule.getId() != null)
                .filter(activeSchedule -> !activeSchedule.getId().equals(targetSchedule.getId()))
                .findFirst()
                .orElse(null);

        int copiedPersonalEntriesCount = copyPersonalEntriesFromPreviousActiveSchedule(
                previousActiveSchedule,
                targetSchedule
        );

        List<ConflictDTO> conflicts = detectConflicts(targetSchedule.getEntries());

        activeSchedules.stream()
                .filter(activeSchedule -> activeSchedule.getId() != null)
                .filter(activeSchedule -> !activeSchedule.getId().equals(targetSchedule.getId()))
                .forEach(activeSchedule -> activeSchedule.setStatus(ScheduleStatus.ARCHIVED));

        targetSchedule.setStatus(ScheduleStatus.ACTIVE);
        targetSchedule.setActivatedAt(LocalDateTime.now());

        scheduleRepository.saveAll(activeSchedules);
        StudentSchedule savedSchedule = scheduleRepository.save(targetSchedule);

        notifyScheduleActivated(
                savedSchedule,
                copiedPersonalEntriesCount,
                conflicts
        );

        log.info(
                "Schedule {} activated for student {}. Old active schedules archived. Copied personal entries: {}. Conflicts: {}",
                scheduleId,
                studentId,
                copiedPersonalEntriesCount,
                conflicts.size()
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

        List<EntrySnapshot> beforeEntries = snapshotEntries(schedule.getEntries());

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

        List<EntrySnapshot> afterEntries = snapshotEntries(newEntries);
        List<String> changeLines = buildScheduleChangeLines(beforeEntries, afterEntries);

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
                buildScheduleUpdateNotificationBody(changeLines, conflicts),
                conflicts.isEmpty()
                        ? NotificationPriority.NORMAL
                        : NotificationPriority.HIGH
        );

        log.info(
                "Schedule {} updated with {} entries, {} changes, {} conflicts",
                scheduleId,
                newEntries.size(),
                changeLines.size(),
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

    private int copyPersonalEntriesFromPreviousActiveSchedule(
            StudentSchedule previousActiveSchedule,
            StudentSchedule targetSchedule
    ) {
        if (previousActiveSchedule == null || targetSchedule == null) {
            return 0;
        }

        if (Objects.equals(previousActiveSchedule.getId(), targetSchedule.getId())) {
            return 0;
        }

        List<ScheduleEntry> entriesToCopy = previousActiveSchedule.getEntries()
                .stream()
                .filter(this::isPersonalEntry)
                .filter(sourceEntry -> !hasEquivalentPersonalEntry(targetSchedule, sourceEntry))
                .map(sourceEntry -> copyPersonalEntry(sourceEntry, targetSchedule))
                .toList();

        for (ScheduleEntry entry : entriesToCopy) {
            targetSchedule.addEntry(entry);
        }

        return entriesToCopy.size();
    }

    private boolean isPersonalEntry(ScheduleEntry entry) {
        return entry != null
                && entry.getSourceType() != null
                && entry.getSourceType() != ScheduleSourceType.COURSE;
    }

    private ScheduleEntry copyPersonalEntry(
            ScheduleEntry source,
            StudentSchedule targetSchedule
    ) {
        return ScheduleEntry.builder()
                .schedule(targetSchedule)
                .student(targetSchedule.getStudent())
                .sourceType(source.getSourceType())
                .courseSection(source.getCourseSection())
                .manualCourse(source.getManualCourse())
                .manualSectionNumber(source.getManualSectionNumber())
                .event(source.getEvent())
                .title(source.getTitle())
                .description(source.getDescription())
                .building(source.getBuilding())
                .room(source.getRoom())
                .location(source.getLocation())
                .startDateTime(source.getStartDateTime())
                .endDateTime(source.getEndDateTime())
                .isAllDay(Boolean.TRUE.equals(source.getIsAllDay()))
                .isLocked(Boolean.TRUE.equals(source.getIsLocked()))
                .colorHex(source.getColorHex())
                .build();
    }

    private boolean hasEquivalentPersonalEntry(
            StudentSchedule targetSchedule,
            ScheduleEntry sourceEntry
    ) {
        return targetSchedule.getEntries()
                .stream()
                .filter(this::isPersonalEntry)
                .anyMatch(existingEntry -> isSamePersonalEntry(existingEntry, sourceEntry));
    }

    private boolean isSamePersonalEntry(
            ScheduleEntry first,
            ScheduleEntry second
    ) {
        if (!Objects.equals(first.getSourceType(), second.getSourceType())) {
            return false;
        }

        Long firstEventId = getEventId(first);
        Long secondEventId = getEventId(second);

        if (firstEventId != null || secondEventId != null) {
            return Objects.equals(firstEventId, secondEventId);
        }

        return Objects.equals(getCourseSectionId(first), getCourseSectionId(second))
                && Objects.equals(getManualCourseId(first), getManualCourseId(second))
                && Objects.equals(first.getManualSectionNumber(), second.getManualSectionNumber())
                && Objects.equals(first.getTitle(), second.getTitle())
                && Objects.equals(first.getDescription(), second.getDescription())
                && Objects.equals(first.getLocation(), second.getLocation())
                && Objects.equals(first.getStartDateTime(), second.getStartDateTime())
                && Objects.equals(first.getEndDateTime(), second.getEndDateTime())
                && Objects.equals(
                Boolean.TRUE.equals(first.getIsAllDay()),
                Boolean.TRUE.equals(second.getIsAllDay())
        );
    }

    private Long getEventId(ScheduleEntry entry) {
        if (entry == null || entry.getEvent() == null) {
            return null;
        }

        return entry.getEvent().getId();
    }

    private Long getManualCourseId(ScheduleEntry entry) {
        if (entry == null || entry.getManualCourse() == null) {
            return null;
        }

        return entry.getManualCourse().getId();
    }

    private Long getCourseSectionId(ScheduleEntry entry) {
        if (entry == null || entry.getCourseSection() == null) {
            return null;
        }

        return entry.getCourseSection().getId();
    }

    private void notifyScheduleActivated(
            StudentSchedule schedule,
            int copiedPersonalEntriesCount,
            List<ConflictDTO> conflicts
    ) {
        String title = conflicts == null || conflicts.isEmpty()
                ? "Schedule activated"
                : "Schedule activated with conflicts";

        StringBuilder body = new StringBuilder();
        body.append("Your new schedule is now active.");

        if (copiedPersonalEntriesCount > 0) {
            body.append("\n\nTransferred entries: ")
                    .append(copiedPersonalEntriesCount)
                    .append(" manual/event entry(s) were moved from your previous active schedule.");
        } else {
            body.append("\n\nTransferred entries: No manual/event entries needed to be moved.");
        }

        appendConflictDetails(body, conflicts);

        notifyScheduleOwner(
                schedule,
                title,
                body.toString(),
                conflicts == null || conflicts.isEmpty()
                        ? NotificationPriority.NORMAL
                        : NotificationPriority.HIGH
        );
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

    private String buildScheduleUpdateNotificationBody(
            List<String> changeLines,
            List<ConflictDTO> conflicts
    ) {
        StringBuilder body = new StringBuilder();

        if (conflicts == null || conflicts.isEmpty()) {
            body.append("Your schedule was updated successfully.");
        } else {
            body.append("Your schedule was updated, but some entries have time conflicts.");
        }

        if (changeLines == null || changeLines.isEmpty()) {
            body.append("\n\nChanged details: No visible entry changes were detected.");
        } else {
            body.append("\n\nChanged details:");

            int maxVisibleChanges = Math.min(changeLines.size(), 8);

            for (int i = 0; i < maxVisibleChanges; i++) {
                body.append("\n- ").append(changeLines.get(i));
            }

            if (changeLines.size() > maxVisibleChanges) {
                body.append("\n- +")
                        .append(changeLines.size() - maxVisibleChanges)
                        .append(" more change(s)");
            }
        }

        appendConflictDetails(body, conflicts);

        return body.toString();
    }

    private void appendConflictDetails(
            StringBuilder body,
            List<ConflictDTO> conflicts
    ) {
        if (body == null || conflicts == null || conflicts.isEmpty()) {
            return;
        }

        body.append("\n\nConflicts:");

        int maxVisibleConflicts = Math.min(conflicts.size(), 6);

        for (int i = 0; i < maxVisibleConflicts; i++) {
            body.append("\n- ").append(formatConflictLine(conflicts.get(i)));
        }

        if (conflicts.size() > maxVisibleConflicts) {
            body.append("\n- +")
                    .append(conflicts.size() - maxVisibleConflicts)
                    .append(" more conflict(s)");
        }
    }

    private String formatConflictLine(ConflictDTO conflict) {
        if (conflict == null) {
            return "Unknown conflict";
        }

        return safeText(conflict.getEntryATitle(), "Entry A")
                + " conflicts with "
                + safeText(conflict.getEntryBTitle(), "Entry B")
                + " from "
                + formatDateTime(conflict.getConflictStart())
                + " to "
                + formatDateTime(conflict.getConflictEnd());
    }

    private List<EntrySnapshot> snapshotEntries(List<ScheduleEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return entries.stream()
                .map(EntrySnapshot::from)
                .sorted(Comparator.comparing(EntrySnapshot::sortKey))
                .toList();
    }

    private List<String> buildScheduleChangeLines(
            List<EntrySnapshot> beforeEntries,
            List<EntrySnapshot> afterEntries
    ) {
        Map<String, EntrySnapshot> beforeMap = toSnapshotMap(beforeEntries);
        Map<String, EntrySnapshot> afterMap = toSnapshotMap(afterEntries);

        List<String> changes = new ArrayList<>();

        for (Map.Entry<String, EntrySnapshot> afterItem : afterMap.entrySet()) {
            EntrySnapshot before = beforeMap.get(afterItem.getKey());
            EntrySnapshot after = afterItem.getValue();

            if (before == null) {
                changes.add("Added: " + after.displayNameWithTime());
                continue;
            }

            List<String> entryChanges = before.diff(after);

            if (!entryChanges.isEmpty()) {
                changes.add("Updated " + after.displayName + ": " + String.join(", ", entryChanges));
            }
        }

        for (Map.Entry<String, EntrySnapshot> beforeItem : beforeMap.entrySet()) {
            if (!afterMap.containsKey(beforeItem.getKey())) {
                changes.add("Removed: " + beforeItem.getValue().displayNameWithTime());
            }
        }

        return changes;
    }

    private Map<String, EntrySnapshot> toSnapshotMap(List<EntrySnapshot> snapshots) {
        Map<String, EntrySnapshot> map = new LinkedHashMap<>();

        if (snapshots == null) {
            return map;
        }

        for (EntrySnapshot snapshot : snapshots) {
            String key = snapshot.stableKey();

            if (map.containsKey(key)) {
                key = key + "#" + snapshot.sortKey();
            }

            map.put(key, snapshot);
        }

        return map;
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

        String displayCode = displayCourseCode(course);

        if (displayCode != null && !displayCode.isBlank()) {
            return displayCode;
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
                .code(displayCourseCode(course))
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

    private String displayCourseCode(Course course) {
        if (course == null) {
            return null;
        }

        if (course.getExternalId() != null && !course.getExternalId().isBlank()) {
            return course.getExternalId().trim();
        }

        if (course.getCode() != null && !course.getCode().isBlank()) {
            return course.getCode().trim();
        }

        return null;
    }

    private static String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }

    private static String cleanText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "not set";
        }

        return dateTime.format(NOTIFICATION_DATE_TIME_FORMATTER);
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "not set";
        }

        if (value instanceof String text) {
            if (text.isBlank()) {
                return "not set";
            }

            return truncate(text.trim(), 60);
        }

        if (value instanceof LocalDateTime dateTime) {
            return formatDateTime(dateTime);
        }

        if (value instanceof Boolean bool) {
            return bool ? "Yes" : "No";
        }

        return truncate(String.valueOf(value), 60);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static final class EntrySnapshot {
        private Long id;
        private ScheduleSourceType sourceType;
        private Long courseSectionId;
        private Long manualCourseId;
        private Long eventId;
        private String manualSectionNumber;
        private String displayName;
        private String description;
        private String location;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private Boolean isAllDay;
        private String colorHex;

        private static EntrySnapshot from(ScheduleEntry entry) {
            EntrySnapshot snapshot = new EntrySnapshot();

            if (entry == null) {
                return snapshot;
            }

            snapshot.id = entry.getId();
            snapshot.sourceType = entry.getSourceType();
            snapshot.courseSectionId = entry.getCourseSection() != null
                    ? entry.getCourseSection().getId()
                    : null;
            snapshot.manualCourseId = entry.getManualCourse() != null
                    ? entry.getManualCourse().getId()
                    : null;
            snapshot.eventId = entry.getEvent() != null
                    ? entry.getEvent().getId()
                    : null;
            snapshot.manualSectionNumber = cleanText(entry.getManualSectionNumber());
            snapshot.displayName = safeText(entry.getTitle(), "Schedule Entry");
            snapshot.description = cleanText(entry.getDescription());
            snapshot.location = cleanText(entry.getLocation());
            snapshot.startDateTime = entry.getStartDateTime();
            snapshot.endDateTime = entry.getEndDateTime();
            snapshot.isAllDay = Boolean.TRUE.equals(entry.getIsAllDay());
            snapshot.colorHex = cleanText(entry.getColorHex());

            return snapshot;
        }

        private String stableKey() {
            if (sourceType == ScheduleSourceType.EVENT && eventId != null) {
                return "EVENT:" + eventId;
            }

            if (sourceType == ScheduleSourceType.COURSE && courseSectionId != null) {
                return "COURSE:" + courseSectionId;
            }

            if (sourceType == ScheduleSourceType.MANUAL && manualCourseId != null) {
                return "MANUAL:"
                        + manualCourseId
                        + ":"
                        + safeText(manualSectionNumber, "NO_SECTION")
                        + ":"
                        + formatDateTime(startDateTime)
                        + ":"
                        + formatDateTime(endDateTime);
            }

            if (id != null) {
                return "ID:" + id;
            }

            return "ENTRY:"
                    + sourceType
                    + ":"
                    + safeText(displayName, "NO_TITLE")
                    + ":"
                    + formatDateTime(startDateTime)
                    + ":"
                    + formatDateTime(endDateTime)
                    + ":"
                    + safeText(location, "NO_LOCATION");
        }

        private String sortKey() {
            return stableKey()
                    + ":"
                    + safeText(displayName, "")
                    + ":"
                    + formatDateTime(startDateTime)
                    + ":"
                    + formatDateTime(endDateTime);
        }

        private String displayNameWithTime() {
            return displayName
                    + " ["
                    + formatDateTime(startDateTime)
                    + " - "
                    + formatDateTime(endDateTime)
                    + "]";
        }

        private List<String> diff(EntrySnapshot after) {
            List<String> changes = new ArrayList<>();

            addDiff(changes, "title", displayName, after.displayName);
            addDiff(changes, "description", description, after.description);
            addDiff(changes, "location", location, after.location);
            addDiff(changes, "start", startDateTime, after.startDateTime);
            addDiff(changes, "end", endDateTime, after.endDateTime);
            addDiff(changes, "all-day", isAllDay, after.isAllDay);
            addDiff(changes, "color", colorHex, after.colorHex);

            return changes;
        }

        private void addDiff(
                List<String> changes,
                String label,
                Object before,
                Object after
        ) {
            if (Objects.equals(before, after)) {
                return;
            }

            changes.add(label + ": " + formatValue(before) + " → " + formatValue(after));
        }
    }
}