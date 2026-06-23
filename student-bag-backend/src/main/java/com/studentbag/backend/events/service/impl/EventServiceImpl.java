package com.studentbag.backend.events.service.impl;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.domain.enums.EventStatus;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.domain.enums.courses.RegistrationStatus;
import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import com.studentbag.backend.domain.enums.schedule.ScheduleSourceType;
import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.events.dto.request.*;
import com.studentbag.backend.events.dto.response.EventRegistrationInfoDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;
import com.studentbag.backend.events.entity.Event;
import com.studentbag.backend.events.entity.EventRegistration;
import com.studentbag.backend.events.entity.Opportunity;
import com.studentbag.backend.events.mapper.EventMapper;
import com.studentbag.backend.events.repository.EventRegistrationRepository;
import com.studentbag.backend.events.repository.EventRepository;
import com.studentbag.backend.events.service.EventService;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.notifications.dto.request.CreateNotificationRequest;
import com.studentbag.backend.notifications.service.NotificationService;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StudentScheduleRepository studentScheduleRepository;
    private final NotificationService notificationService;

    @Override
    public EventResponseDTO createEvent(
            EventRequestDTO request,
            Long institutionId,
            String currentUserEmail
    ) {
        validateEventDates(request);

        Institution institution = getInstitutionById(institutionId);
        User creator = getUserByEmail(currentUserEmail);

        Event event = eventMapper.toEntity(request);
        event.setInstitution(institution);
        event.setCreatedByUser(creator);
        event.setStatus(EventStatus.ACTIVE);
        event.setDeletedAt(null);

        if (event.getHost() == null || event.getHost().isBlank()) {
            event.setHost(creator.getFullName());
        }

        applyOpportunityDetails(event, request);

        Event savedEvent = eventRepository.save(event);

        log.info(
                "Created event id={} institutionId={} createdByUser={} status={} isOpportunity={} hasOpportunity={}",
                savedEvent.getId(),
                institutionId,
                creator.getId(),
                savedEvent.getStatus(),
                savedEvent.getIsOpportunity(),
                savedEvent.getOpportunity() != null
        );

        return buildEventResponse(savedEvent, null);
    }

    @Override
    public EventResponseDTO updateEvent(
            Long eventId,
            EventRequestDTO request,
            Long institutionId,
            String currentUserEmail
    ) {
        validateEventDates(request);

        User currentUser = getUserByEmail(currentUserEmail);
        Event existingEvent = getEventByIdOrThrow(eventId);
        Institution institution = getInstitutionById(institutionId);

        validateEventOwnerOrAdmin(existingEvent, currentUser);

        if (resolveEventStatus(existingEvent) == EventStatus.DELETED) {
            throw new IllegalStateException("Cannot update a deleted event");
        }

        EventChangeSnapshot beforeUpdate = EventChangeSnapshot.from(existingEvent);

        EventStatus previousStatus = existingEvent.getStatus() != null
                ? existingEvent.getStatus()
                : EventStatus.ACTIVE;
        LocalDateTime previousDeletedAt = existingEvent.getDeletedAt();

        eventMapper.updateEntity(existingEvent, request);
        existingEvent.setInstitution(institution);

        // Normal edit must not change lifecycle state.
        existingEvent.setStatus(previousStatus);
        existingEvent.setDeletedAt(previousDeletedAt);

        if (existingEvent.getCreatedByUser() == null) {
            existingEvent.setCreatedByUser(currentUser);
        }

        if (existingEvent.getHost() == null || existingEvent.getHost().isBlank()) {
            existingEvent.setHost(
                    existingEvent.getCreatedByUser() != null
                            ? existingEvent.getCreatedByUser().getFullName()
                            : currentUser.getFullName()
            );
        }

        applyOpportunityDetails(existingEvent, request);

        Event savedEvent = eventRepository.save(existingEvent);

        EventChangeSnapshot afterUpdate = EventChangeSnapshot.from(savedEvent);
        List<String> changedDetails = buildEventChangeLines(beforeUpdate, afterUpdate);

        refreshEventEntriesInActiveSchedules(savedEvent);

        notifyEventUpdated(savedEvent, currentUser, changedDetails);

        log.info(
                "Updated event id={} updatedByUser={} status={} isOpportunity={} hasOpportunity={} changedDetailsCount={}",
                savedEvent.getId(),
                currentUser.getId(),
                savedEvent.getStatus(),
                savedEvent.getIsOpportunity(),
                savedEvent.getOpportunity() != null,
                changedDetails.size()
        );

        return buildEventResponse(savedEvent, null);
    }

    @Override
    public EventResponseDTO finishEvent(Long eventId, String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        Event event = getEventByIdOrThrow(eventId);

        validateEventOwnerOrAdmin(event, currentUser);

        EventStatus status = resolveEventStatus(event);

        if (status == EventStatus.DELETED) {
            throw new IllegalStateException("Cannot finish a deleted event");
        }

        if (status == EventStatus.CANCELLED) {
            throw new IllegalStateException("Cannot finish a cancelled event. Reopen it first.");
        }

        event.setStatus(EventStatus.FINISHED);
        event.setDeletedAt(null);
        event.setEndDateTime(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        removeEventEntriesFromRegisteredStudentsActiveSchedules(savedEvent);

        notifyRegisteredStudents(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity finished" : "Event finished",
                "The " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" has finished.",
                studentEventRoute(savedEvent)
        );

        notifyEventCreator(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity finished" : "Event finished",
                "Your " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" has finished."
        );

        log.info("Finished event id={} by user={}", savedEvent.getId(), currentUser.getId());

        return buildEventResponse(savedEvent, null);
    }

    @Override
    public EventResponseDTO cancelEvent(Long eventId, String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        Event event = getEventByIdOrThrow(eventId);

        validateEventOwnerOrAdmin(event, currentUser);

        EventStatus status = resolveEventStatus(event);

        if (status == EventStatus.DELETED) {
            throw new IllegalStateException("Cannot cancel a deleted event");
        }

        if (status == EventStatus.CANCELLED) {
            throw new IllegalStateException("Event is already cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setDeletedAt(null);

        if (event.getEndDateTime() == null || event.getEndDateTime().isAfter(LocalDateTime.now())) {
            event.setEndDateTime(LocalDateTime.now());
        }

        Event savedEvent = eventRepository.save(event);

        removeEventEntriesFromRegisteredStudentsActiveSchedules(savedEvent);

        notifyRegisteredStudents(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity cancelled" : "Event cancelled",
                "The " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" was cancelled.",
                studentEventRoute(savedEvent)
        );

        notifyEventCreator(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity cancelled" : "Event cancelled",
                "Your " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" was cancelled."
        );

        log.info("Cancelled event id={} by user={}", savedEvent.getId(), currentUser.getId());

        return buildEventResponse(savedEvent, null);
    }

    @Override
    public void deleteEvent(Long eventId) {
        Event event = getEventByIdOrThrow(eventId);

        if (resolveEventStatus(event) == EventStatus.DELETED) {
            log.info("Event id={} is already deleted", eventId);
            return;
        }

        removeEventEntriesFromRegisteredStudentsActiveSchedules(event);

        // Soft delete: keep registrations/history so admin Deleted tab can show it.
        event.setStatus(EventStatus.DELETED);
        event.setDeletedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        notifyRegisteredStudents(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity removed" : "Event removed",
                "The " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" was removed by the administration.",
                studentCollectionRoute(savedEvent)
        );

        notifyEventCreator(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity removed" : "Event removed",
                "Your " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" was removed."
        );

        log.info("Soft deleted event id={}", eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long eventId, Long studentId) {
        Event event = getEventByIdOrThrow(eventId);
        return buildEventResponse(event, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents(Long studentId) {
        return eventRepository.findAll()
                .stream()
                .filter(event -> isVisibleForRequester(event, studentId))
                .sorted(Comparator.comparing(
                        Event::getStartDateTime,
                        Comparator.nullsLast(LocalDateTime::compareTo)
                ))
                .map(event -> buildEventResponse(event, studentId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> searchEvents(Long studentId, EventSearchRequestDTO request) {
        return eventRepository.findAll()
                .stream()
                .filter(event -> isVisibleForRequester(event, studentId))
                .filter(event -> matchesSearch(event, request))
                .sorted(buildComparator(request))
                .map(event -> buildEventResponse(event, studentId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponseDTO> searchOpportunities(
            Long studentId,
            EventSearchRequestDTO request
    ) {
        return eventRepository.findAll()
                .stream()
                .filter(event -> isVisibleForRequester(event, studentId))
                .filter(this::isOpportunityEvent)
                .filter(event -> matchesSearch(event, request))
                .sorted(buildComparator(request))
                .map(Event::getOpportunity)
                .filter(Objects::nonNull)
                .map(eventMapper::toOpportunityResponseDTO)
                .toList();
    }

    @Override
    public void registerForEvent(Long eventId, Long studentId) {
        registerForEvent(eventId, studentId, null);
    }

    @Override
    public void registerForEvent(
            Long eventId,
            Long studentId,
            EventRegistrationRequestDTO request
    ) {
        Event event = getEventByIdOrThrow(eventId);
        Student student = getStudentById(studentId);

        validateRegistration(event, studentId);

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .student(student)
                .build();

        registration.register();
        registrationRepository.save(registration);

        addEventEntryToActiveSchedule(event, student);

        notifyStudent(
                student,
                isOpportunityEvent(event)
                        ? "Opportunity registration confirmed"
                        : "Event registration confirmed",
                "You are registered for \"" + safeTitle(event) + "\".",
                studentEventRoute(event)
        );

        notifyEventCreator(
                event,
                isOpportunityEvent(event)
                        ? "New opportunity registration"
                        : "New event registration",
                studentName(student) + " registered for \"" + safeTitle(event) + "\"."
        );

        log.info("Student {} registered for event {}", studentId, eventId);
    }

    @Override
    public void cancelRegistration(Long eventId, Long studentId) {
        EventRegistration registration = registrationRepository
                .findByEventIdAndStudentId(eventId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for event id: " + eventId
                                + " and student id: " + studentId
                ));

        Event event = registration.getEvent();
        Student student = registration.getStudent();

        registrationRepository.delete(registration);

        removeEventEntryFromActiveSchedule(eventId, studentId);

        notifyStudent(
                student,
                isOpportunityEvent(event)
                        ? "Opportunity registration cancelled"
                        : "Event registration cancelled",
                "Your registration for \"" + safeTitle(event) + "\" was cancelled.",
                studentEventRoute(event)
        );

        notifyEventCreator(
                event,
                isOpportunityEvent(event)
                        ? "Opportunity registration cancelled"
                        : "Event registration cancelled",
                studentName(student) + " cancelled registration for \"" + safeTitle(event) + "\"."
        );

        log.info("Student {} cancelled registration for event {}", studentId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationInfoDTO> getEventRegistrations(
            Long eventId,
            String currentUserEmail
    ) {
        Event event = getEventByIdOrThrow(eventId);
        User currentUser = getUserByEmail(currentUserEmail);

        validateEventOwnerOrAdmin(event, currentUser);

        return registrationRepository.findAllByEventId(eventId)
                .stream()
                .map(this::mapRegistrationInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getEventRegistrationCount(
            Long eventId,
            String currentUserEmail
    ) {
        Event event = getEventByIdOrThrow(eventId);
        User currentUser = getUserByEmail(currentUserEmail);

        validateEventOwnerOrAdmin(event, currentUser);

        return registrationRepository.countByEventIdAndStatusIn(
                eventId,
                List.of(RegistrationStatus.REGISTERED, RegistrationStatus.CHECKED_IN)
        );
    }

    @Override
    public void syncWithUniversityAPI(Long institutionId) {
        log.info("Sync with university API requested for institutionId={}", institutionId);
    }

    @Override
    public void notifyEventRegistrants(
            Long eventId,
            EventRegistrantsNotificationRequestDTO request,
            String currentUserEmail
    ) {
        Event event = getEventByIdOrThrow(eventId);
        User currentUser = getUserByEmail(currentUserEmail);

        validateEventOwnerOrAdmin(event, currentUser);

        String message = request.getMessage() == null
                ? ""
                : request.getMessage().trim();

        if (message.isBlank()) {
            throw new IllegalArgumentException("Notification message must not be blank");
        }

        if (message.length() < 3) {
            throw new IllegalArgumentException("Notification message is too short");
        }

        if (message.length() > 500) {
            throw new IllegalArgumentException("Notification message is too long");
        }

        String title = isOpportunityEvent(event)
                ? "Opportunity update"
                : "Event update";

        notifyRegisteredStudents(
                event,
                title + ": " + safeTitle(event),
                message,
                studentEventRoute(event)
        );

        log.info(
                "Manual notification sent to registered students for event id={} by user={}",
                eventId,
                currentUser.getId()
        );
    }

    @Override
    public EventResponseDTO reopenEvent(
            Long eventId,
            EventReopenRequestDTO request,
            String currentUserEmail
    ) {
        User currentUser = getUserByEmail(currentUserEmail);
        Event event = getEventByIdOrThrow(eventId);

        validateEventOwnerOrAdmin(event, currentUser);

        if (request == null || request.getEndDateTime() == null) {
            throw new IllegalArgumentException("New end date/time is required to reopen the event");
        }

        LocalDateTime now = LocalDateTime.now();

        if (!request.getEndDateTime().isAfter(now)) {
            throw new IllegalArgumentException("New end date/time must be in the future");
        }

        LocalDateTime newStartDateTime = request.getStartDateTime() != null
                ? request.getStartDateTime()
                : event.getStartDateTime();

        if (newStartDateTime == null) {
            throw new IllegalArgumentException("Start date/time is required to reopen the event");
        }

        if (request.getEndDateTime().isBefore(newStartDateTime)) {
            throw new IllegalArgumentException("End date/time must be after start date/time");
        }

        event.setStartDateTime(newStartDateTime);
        event.setEndDateTime(request.getEndDateTime());
        event.setStatus(EventStatus.ACTIVE);
        event.setDeletedAt(null);

        Event savedEvent = eventRepository.save(event);

        refreshEventEntriesInActiveSchedules(savedEvent);

        notifyRegisteredStudents(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity reopened" : "Event reopened",
                "The " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" has been reopened.",
                studentEventRoute(savedEvent)
        );

        notifyEventCreator(
                savedEvent,
                isOpportunityEvent(savedEvent) ? "Opportunity reopened" : "Event reopened",
                "Your " + eventKind(savedEvent) + " \"" + safeTitle(savedEvent) + "\" has been reopened."
        );

        log.info("Reopened event id={} by user={}", savedEvent.getId(), currentUser.getId());

        return buildEventResponse(savedEvent, null);
    }

    private void addEventEntryToActiveSchedule(Event event, Student student) {
        if (event == null || student == null || student.getId() == null) {
            return;
        }

        if (!canAppearInActiveSchedule(event)) {
            return;
        }

        if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
            log.warn("Cannot add event {} to active schedule because date/time is missing.", event.getId());
            return;
        }

        StudentSchedule activeSchedule = studentScheduleRepository
                .findFirstByStudent_IdAndStatusOrderByActivatedAtDescCreatedAtDesc(
                        student.getId(),
                        ScheduleStatus.ACTIVE
                )
                .orElse(null);

        if (activeSchedule == null) {
            log.warn(
                    "Student {} registered for event {}, but no active schedule was found.",
                    student.getId(),
                    event.getId()
            );
            return;
        }

        boolean alreadyExists = activeSchedule.getEntries()
                .stream()
                .anyMatch(entry ->
                        entry.getSourceType() == ScheduleSourceType.EVENT
                                && entry.getEvent() != null
                                && Objects.equals(entry.getEvent().getId(), event.getId())
                );

        if (alreadyExists) {
            return;
        }

        ScheduleEntry entry = ScheduleEntry.builder()
                .schedule(activeSchedule)
                .student(student)
                .event(event)
                .sourceType(ScheduleSourceType.EVENT)
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .isAllDay(false)
                .isLocked(true)
                .colorHex("#2563EB")
                .build();

        activeSchedule.addEntry(entry);
        studentScheduleRepository.save(activeSchedule);
    }

    private void removeEventEntryFromActiveSchedule(Long eventId, Long studentId) {
        StudentSchedule activeSchedule = studentScheduleRepository
                .findFirstByStudent_IdAndStatusOrderByActivatedAtDescCreatedAtDesc(
                        studentId,
                        ScheduleStatus.ACTIVE
                )
                .orElse(null);

        if (activeSchedule == null) {
            return;
        }

        activeSchedule.getEntries().removeIf(entry ->
                entry.getSourceType() == ScheduleSourceType.EVENT
                        && entry.getEvent() != null
                        && Objects.equals(entry.getEvent().getId(), eventId)
        );

        studentScheduleRepository.save(activeSchedule);
    }

    private void removeEventEntriesFromRegisteredStudentsActiveSchedules(Event event) {
        if (event == null || event.getId() == null) {
            return;
        }

        List<EventRegistration> registrations =
                registrationRepository.findAllByEventId(event.getId());

        for (EventRegistration registration : registrations) {
            if (registration.getStudent() == null || registration.getStudent().getId() == null) {
                continue;
            }

            removeEventEntryFromActiveSchedule(
                    event.getId(),
                    registration.getStudent().getId()
            );
        }
    }

    private void refreshEventEntriesInActiveSchedules(Event event) {
        if (event == null || event.getId() == null) {
            return;
        }

        List<EventRegistration> registrations =
                registrationRepository.findAllByEventId(event.getId());

        for (EventRegistration registration : registrations) {
            Student student = registration.getStudent();

            if (student == null || student.getId() == null) {
                continue;
            }

            removeEventEntryFromActiveSchedule(event.getId(), student.getId());

            if (canAppearInActiveSchedule(event)) {
                addEventEntryToActiveSchedule(event, student);
            }
        }
    }

    private void notifyEventUpdated(
            Event event,
            User updatedBy,
            List<String> changedDetails
    ) {
        if (event == null || event.getId() == null || updatedBy == null) {
            return;
        }

        boolean updatedByAdmin = updatedBy.getRole() == UserRole.ADMINISTRATOR;
        boolean updatedByCreator = event.getCreatedByUser() != null
                && Objects.equals(event.getCreatedByUser().getId(), updatedBy.getId());

        String baseStudentBody = updatedByAdmin
                ? "The " + eventKind(event) + " \"" + safeTitle(event) + "\" has been updated by the administration."
                : "The " + eventKind(event) + " \"" + safeTitle(event) + "\" has been updated.";

        String changesSummary = buildChangesSummary(changedDetails);

        notifyRegisteredStudents(
                event,
                capitalize(eventKind(event)) + " updated: " + safeTitle(event),
                baseStudentBody + changesSummary,
                studentEventRoute(event)
        );

        if (updatedByAdmin && !updatedByCreator) {
            notifyEventCreator(
                    event,
                    "Your " + eventKind(event) + " was updated by admin",
                    "An administrator updated your " + eventKind(event) + " \""
                            + safeTitle(event) + "\"." + changesSummary
            );
        }
    }

    private List<String> buildEventChangeLines(
            EventChangeSnapshot before,
            EventChangeSnapshot after
    ) {
        List<String> changes = new ArrayList<>();

        if (before == null || after == null) {
            return changes;
        }

        addChange(changes, "Title", before.title, after.title);
        addChange(changes, "Description", before.description, after.description);
        addChange(changes, "Location", before.location, after.location);
        addChange(changes, "Department", before.department, after.department);
        addChange(changes, "Host", before.host, after.host);
        addChange(changes, "Event type", before.eventType, after.eventType);
        addChange(changes, "Start time", before.startDateTime, after.startDateTime);
        addChange(changes, "End time", before.endDateTime, after.endDateTime);
        addChange(changes, "Requires registration", before.requiresRegistration, after.requiresRegistration);
        addChange(changes, "Opportunity", before.isOpportunity, after.isOpportunity);
        addChange(changes, "Company name", before.opportunityCompanyName, after.opportunityCompanyName);
        addChange(changes, "Role title", before.opportunityRoleTitle, after.opportunityRoleTitle);
        addChange(changes, "Opportunity field", before.opportunityField, after.opportunityField);
        addChange(changes, "Paid opportunity", before.opportunityIsPaid, after.opportunityIsPaid);
        addChange(changes, "Work mode", before.opportunityWorkMode, after.opportunityWorkMode);
        addChange(changes, "Application deadline", before.opportunityApplicationDeadline, after.opportunityApplicationDeadline);
        addChange(changes, "Duration weeks", before.opportunityDurationWeeks, after.opportunityDurationWeeks);
        addChange(changes, "Application URL", before.opportunityApplicationUrl, after.opportunityApplicationUrl);

        return changes;
    }

    private void addChange(
            List<String> changes,
            String label,
            Object oldValue,
            Object newValue
    ) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        changes.add(label + ": " + formatNotificationValue(oldValue)
                + " → " + formatNotificationValue(newValue));
    }

    private String buildChangesSummary(List<String> changedDetails) {
        if (changedDetails == null || changedDetails.isEmpty()) {
            return " No visible details were changed.";
        }

        int maxVisibleChanges = 6;
        StringBuilder builder = new StringBuilder();
        builder.append("\n\nChanged details:");

        int visibleCount = Math.min(changedDetails.size(), maxVisibleChanges);

        for (int i = 0; i < visibleCount; i++) {
            builder.append("\n- ").append(changedDetails.get(i));
        }

        if (changedDetails.size() > maxVisibleChanges) {
            builder.append("\n- +")
                    .append(changedDetails.size() - maxVisibleChanges)
                    .append(" more change(s)");
        }

        return builder.toString();
    }

    private String formatNotificationValue(Object value) {
        if (value == null) {
            return "not set";
        }

        if (value instanceof String text) {
            if (text.isBlank()) {
                return "not set";
            }

            return truncateForNotification(text.trim(), 80);
        }

        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toString().replace("T", " ");
        }

        if (value instanceof LocalDate date) {
            return date.toString();
        }

        if (value instanceof Boolean bool) {
            return bool ? "Yes" : "No";
        }

        return truncateForNotification(String.valueOf(value), 80);
    }

    private String truncateForNotification(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String studentEventRoute(Event event) {
        if (event == null || event.getId() == null) {
            return "/student/events";
        }

        if (isOpportunityEvent(event)) {
            return "/student/opportunities/" + event.getId();
        }

        return "/student/events/" + event.getId();
    }

    private String studentCollectionRoute(Event event) {
        if (isOpportunityEvent(event)) {
            return "/student/opportunities";
        }

        return "/student/events";
    }

    private void notifyRegisteredStudents(
            Event event,
            String title,
            String body,
            String route
    ) {
        if (event == null || event.getId() == null) {
            return;
        }

        List<EventRegistration> registrations =
                registrationRepository.findAllByEventId(event.getId());

        List<UUID> recipientUserIds = registrations.stream()
                .map(EventRegistration::getStudent)
                .filter(Objects::nonNull)
                .map(Student::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (recipientUserIds.isEmpty()) {
            return;
        }

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle(title);
        request.setBody(body);
        request.setType(NotificationType.EVENT);
        request.setPriority(NotificationPriority.HIGH);
        request.setChannel(NotificationChannel.BOTH);
        request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
        request.setTargetValue(route);
        request.setBroadcastToAll(false);
        request.setRecipientUserIds(recipientUserIds);

        notificationService.createAndSend(request);
    }

    private void notifyStudent(
            Student student,
            String title,
            String body,
            String route
    ) {
        if (student == null || student.getUser() == null || student.getUser().getId() == null) {
            return;
        }

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle(title);
        request.setBody(body);
        request.setType(NotificationType.EVENT);
        request.setPriority(NotificationPriority.NORMAL);
        request.setChannel(NotificationChannel.BOTH);
        request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
        request.setTargetValue(route);
        request.setBroadcastToAll(false);
        request.setRecipientUserIds(List.of(student.getUser().getId()));

        notificationService.createAndSend(request);
    }

    private void notifyEventCreator(
            Event event,
            String title,
            String body
    ) {
        if (event == null
                || event.getCreatedByUser() == null
                || event.getCreatedByUser().getId() == null) {
            return;
        }

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle(title);
        request.setBody(body);
        request.setType(NotificationType.EVENT);
        request.setPriority(NotificationPriority.NORMAL);
        request.setChannel(NotificationChannel.BOTH);
        request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
        request.setTargetValue(eventOwnerRoute(event));
        request.setBroadcastToAll(false);
        request.setRecipientUserIds(List.of(event.getCreatedByUser().getId()));

        notificationService.createAndSend(request);
    }

    private String eventOwnerRoute(Event event) {
        if (event == null || event.getId() == null || event.getCreatedByUser() == null) {
            return "/instructor/events";
        }

        if (event.getCreatedByUser().getRole() == UserRole.ADMINISTRATOR) {
            return "/admin/events/" + event.getId();
        }

        return "/instructor/events/" + event.getId();
    }

    private EventRegistrationInfoDTO mapRegistrationInfo(EventRegistration registration) {
        Student student = registration.getStudent();
        User user = student != null ? student.getUser() : null;

        return EventRegistrationInfoDTO.builder()
                .registrationId(registration.getId())
                .studentId(student != null ? student.getId() : null)
                .userId(user != null ? user.getId() : null)
                .studentName(user != null ? user.getFullName() : null)
                .studentEmail(user != null ? user.getEmail() : null)
                .status(registration.getStatus())
                .build();
    }

    private void validateEventOwnerOrAdmin(Event event, User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user is required");
        }

        if (currentUser.getRole() == UserRole.ADMINISTRATOR) {
            return;
        }

        if (event.getCreatedByUser() == null
                || !Objects.equals(event.getCreatedByUser().getId(), currentUser.getId())) {
            throw new IllegalArgumentException("You are not allowed to manage this event");
        }
    }

    private Institution getInstitutionById(Long institutionId) {
        return institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found with id: " + institutionId
                ));
    }

    private Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + studentId
                ));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email
                ));
    }

    private Event getEventByIdOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + eventId
                ));
    }

    private void validateEventDates(EventRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Event request is required");
        }

        if (request.getStartDateTime() != null
                && request.getEndDateTime() != null
                && request.getEndDateTime().isBefore(request.getStartDateTime())) {
            throw new IllegalArgumentException("End date/time must be after start date/time");
        }
    }

    private void applyOpportunityDetails(Event event, EventRequestDTO request) {
        boolean markedAsOpportunity = Boolean.TRUE.equals(request.getIsOpportunity());
        boolean hasOpportunityDetails = request.getOpportunityDetails() != null;
        boolean shouldBeOpportunity = markedAsOpportunity || hasOpportunityDetails;

        event.setIsOpportunity(shouldBeOpportunity);

        if (!shouldBeOpportunity) {
            Opportunity oldOpportunity = event.getOpportunity();
            if (oldOpportunity != null) {
                oldOpportunity.setEvent(null);
            }

            event.setOpportunity(null);
            return;
        }

        if (!hasOpportunityDetails) {
            return;
        }

        Opportunity opportunity = event.getOpportunity();

        if (opportunity == null) {
            opportunity = new Opportunity();
            opportunity.setEvent(event);
        }

        opportunity.setCompanyName(clean(request.getOpportunityDetails().getCompanyName()));
        opportunity.setRoleTitle(clean(request.getOpportunityDetails().getRoleTitle()));
        opportunity.setField(clean(request.getOpportunityDetails().getField()));
        opportunity.setIsPaid(Boolean.TRUE.equals(request.getOpportunityDetails().getIsPaid()));
        opportunity.setWorkMode(clean(request.getOpportunityDetails().getWorkMode()));
        opportunity.setApplicationDeadline(request.getOpportunityDetails().getApplicationDeadline());
        opportunity.setDurationWeeks(request.getOpportunityDetails().getDurationWeeks());
        opportunity.setApplicationUrl(clean(request.getOpportunityDetails().getApplicationUrl()));

        event.setOpportunity(opportunity);
    }

    private EventResponseDTO buildEventResponse(Event event, Long studentId) {
        EventResponseDTO response = eventMapper.toResponseDTO(event, studentId);
        EventStatus status = resolveEventStatus(event);

        response.setStatus(status);
        response.setIsEnded(isEventEnded(event));
        response.setIsCancelled(status == EventStatus.CANCELLED);
        response.setIsDeleted(status == EventStatus.DELETED);
        response.setDeletedAt(event != null ? event.getDeletedAt() : null);

        if (studentId == null) {
            return response;
        }

        registrationRepository.findByEventIdAndStudentId(event.getId(), studentId)
                .ifPresent(registration -> eventMapper.applyRegistrationInfo(response, registration));

        return response;
    }

    private void validateRegistration(Event event, Long studentId) {
        if (resolveEventStatus(event) != EventStatus.ACTIVE) {
            throw new IllegalStateException("Cannot register for an event that is not active");
        }

        if (Boolean.FALSE.equals(event.getRequiresRegistration())) {
            throw new IllegalStateException("This event does not require registration");
        }

        if (isEventEnded(event)) {
            throw new IllegalStateException("Cannot register for an event that has already ended");
        }

        if (!event.hasCapacity()) {
            throw new IllegalStateException("Event is already at full capacity");
        }

        if (registrationRepository.findByEventIdAndStudentId(event.getId(), studentId).isPresent()) {
            throw new IllegalStateException("Student is already registered for this event");
        }
    }

    private boolean isEventEnded(Event event) {
        EventStatus status = resolveEventStatus(event);

        if (status == EventStatus.FINISHED
                || status == EventStatus.CANCELLED
                || status == EventStatus.DELETED) {
            return true;
        }

        return event != null
                && event.getEndDateTime() != null
                && !event.getEndDateTime().isAfter(LocalDateTime.now());
    }

    private boolean hasEndedByDateTime(Event event) {
        return event != null
                && event.getEndDateTime() != null
                && !event.getEndDateTime().isAfter(LocalDateTime.now());
    }

    private boolean canAppearInActiveSchedule(Event event) {
        return resolveEventStatus(event) == EventStatus.ACTIVE
                && !hasEndedByDateTime(event);
    }

    private EventStatus resolveEventStatus(Event event) {
        if (event == null) {
            return EventStatus.DELETED;
        }

        EventStatus storedStatus = event.getStatus() != null
                ? event.getStatus()
                : EventStatus.ACTIVE;

        if (storedStatus == EventStatus.DELETED
                || storedStatus == EventStatus.CANCELLED
                || storedStatus == EventStatus.FINISHED) {
            return storedStatus;
        }

        if (hasEndedByDateTime(event)) {
            return EventStatus.FINISHED;
        }

        return EventStatus.ACTIVE;
    }

    private boolean isVisibleForRequester(Event event, Long studentId) {
        if (studentId == null) {
            return true;
        }

        return resolveEventStatus(event) != EventStatus.DELETED;
    }

    private boolean isOpportunityEvent(Event event) {
        return Boolean.TRUE.equals(event.getIsOpportunity())
                || event.getOpportunity() != null;
    }

    private boolean matchesSearch(Event event, EventSearchRequestDTO request) {
        if (request == null) {
            return true;
        }

        Opportunity opportunity = event.getOpportunity();

        boolean queryMatches =
                isBlank(request.getQuery())
                        || containsIgnoreCase(event.getTitle(), request.getQuery())
                        || containsIgnoreCase(event.getDescription(), request.getQuery())
                        || containsIgnoreCase(event.getLocation(), request.getQuery())
                        || containsIgnoreCase(event.getDepartment(), request.getQuery())
                        || containsIgnoreCase(event.getHost(), request.getQuery())
                        || (event.getCreatedByUser() != null
                        && containsIgnoreCase(event.getCreatedByUser().getFullName(), request.getQuery()))
                        || (opportunity != null && (
                        containsIgnoreCase(opportunity.getCompanyName(), request.getQuery())
                                || containsIgnoreCase(opportunity.getRoleTitle(), request.getQuery())
                                || containsIgnoreCase(opportunity.getField(), request.getQuery())
                ));

        boolean eventTypeMatches =
                request.getEventType() == null
                        || request.getEventType() == event.getEventType();

        boolean upcomingMatches =
                request.getUpcomingOnly() == null
                        || !request.getUpcomingOnly()
                        || resolveEventStatus(event) == EventStatus.ACTIVE;

        boolean registrationMatches =
                request.getRequiresRegistration() == null
                        || Objects.equals(event.getRequiresRegistration(), request.getRequiresRegistration());

        boolean opportunityMatches =
                request.getIsOpportunity() == null
                        || Objects.equals(isOpportunityEvent(event), request.getIsOpportunity());

        boolean departmentMatches =
                isBlank(request.getDepartment())
                        || containsIgnoreCase(event.getDepartment(), request.getDepartment());

        boolean hostMatches =
                isBlank(request.getHost())
                        || containsIgnoreCase(event.getHost(), request.getHost());

        boolean startDateMatches = matchesStartDateRange(event, request);
        boolean opportunityMatchesExtra = matchesOpportunityFilters(opportunity, request);

        return queryMatches
                && eventTypeMatches
                && upcomingMatches
                && registrationMatches
                && opportunityMatches
                && departmentMatches
                && hostMatches
                && startDateMatches
                && opportunityMatchesExtra;
    }

    private boolean matchesStartDateRange(Event event, EventSearchRequestDTO request) {
        if (event.getStartDateTime() == null) {
            return request.getStartDateFrom() == null
                    && request.getStartDateTo() == null;
        }

        LocalDate startDate = event.getStartDateTime().toLocalDate();

        boolean fromMatches =
                request.getStartDateFrom() == null
                        || !startDate.isBefore(request.getStartDateFrom());

        boolean toMatches =
                request.getStartDateTo() == null
                        || !startDate.isAfter(request.getStartDateTo());

        return fromMatches && toMatches;
    }

    private boolean matchesOpportunityFilters(Opportunity opportunity, EventSearchRequestDTO request) {
        if (opportunity == null) {
            return request.getApplicationOpenOnly() == null
                    && request.getPaidOnly() == null
                    && isBlank(request.getWorkMode())
                    && request.getApplicationDeadlineFrom() == null
                    && request.getApplicationDeadlineTo() == null;
        }

        boolean appOpenMatches =
                request.getApplicationOpenOnly() == null
                        || !request.getApplicationOpenOnly()
                        || opportunity.isApplicationOpen();

        boolean paidMatches =
                request.getPaidOnly() == null
                        || !request.getPaidOnly()
                        || Boolean.TRUE.equals(opportunity.getIsPaid());

        boolean workModeMatches =
                isBlank(request.getWorkMode())
                        || containsIgnoreCase(opportunity.getWorkMode(), request.getWorkMode());

        boolean deadlineMatches = matchesApplicationDeadlineRange(opportunity, request);

        return appOpenMatches
                && paidMatches
                && workModeMatches
                && deadlineMatches;
    }

    private boolean matchesApplicationDeadlineRange(
            Opportunity opportunity,
            EventSearchRequestDTO request
    ) {
        if (opportunity.getApplicationDeadline() == null) {
            return request.getApplicationDeadlineFrom() == null
                    && request.getApplicationDeadlineTo() == null;
        }

        LocalDate deadline = opportunity.getApplicationDeadline();

        boolean fromMatches =
                request.getApplicationDeadlineFrom() == null
                        || !deadline.isBefore(request.getApplicationDeadlineFrom());

        boolean toMatches =
                request.getApplicationDeadlineTo() == null
                        || !deadline.isAfter(request.getApplicationDeadlineTo());

        return fromMatches && toMatches;
    }

    private Comparator<Event> buildComparator(EventSearchRequestDTO request) {
        String sortBy = request != null ? request.getSortBy() : null;
        String sortDirection = request != null ? request.getSortDirection() : null;

        Comparator<Event> comparator;

        String normalizedSortBy = isBlank(sortBy)
                ? "startDateTime"
                : sortBy.trim().toLowerCase();

        switch (normalizedSortBy) {
            case "title" -> comparator = Comparator.comparing(
                    Event::getTitle,
                    Comparator.nullsLast(String::compareToIgnoreCase)
            );

            case "createdat" -> comparator = Comparator.comparing(
                    Event::getCreatedAt,
                    Comparator.nullsLast(LocalDateTime::compareTo)
            );

            case "applicationdeadline" -> comparator = Comparator.comparing(
                    event -> event.getOpportunity() != null
                            ? event.getOpportunity().getApplicationDeadline()
                            : null,
                    Comparator.nullsLast(LocalDate::compareTo)
            );

            default -> comparator = Comparator.comparing(
                    Event::getStartDateTime,
                    Comparator.nullsLast(LocalDateTime::compareTo)
            );
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private String safeTitle(Event event) {
        if (event == null || event.getTitle() == null || event.getTitle().isBlank()) {
            return "Event";
        }

        return event.getTitle();
    }

    private String studentName(Student student) {
        if (student == null || student.getUser() == null) {
            return "A student";
        }

        if (student.getUser().getFullName() != null && !student.getUser().getFullName().isBlank()) {
            return student.getUser().getFullName();
        }

        return "A student";
    }

    private String eventKind(Event event) {
        return isOpportunityEvent(event) ? "opportunity" : "event";
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null
                && query != null
                && value.toLowerCase().contains(query.trim().toLowerCase());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static final class EventChangeSnapshot {
        private String title;
        private String description;
        private String location;
        private String department;
        private String host;
        private Object eventType;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private Boolean requiresRegistration;
        private Boolean isOpportunity;
        private String opportunityCompanyName;
        private String opportunityRoleTitle;
        private String opportunityField;
        private Boolean opportunityIsPaid;
        private String opportunityWorkMode;
        private LocalDate opportunityApplicationDeadline;
        private Integer opportunityDurationWeeks;
        private String opportunityApplicationUrl;

        private static EventChangeSnapshot from(Event event) {
            EventChangeSnapshot snapshot = new EventChangeSnapshot();

            if (event == null) {
                return snapshot;
            }

            snapshot.title = normalizeText(event.getTitle());
            snapshot.description = normalizeText(event.getDescription());
            snapshot.location = normalizeText(event.getLocation());
            snapshot.department = normalizeText(event.getDepartment());
            snapshot.host = normalizeText(event.getHost());
            snapshot.eventType = event.getEventType();
            snapshot.startDateTime = event.getStartDateTime();
            snapshot.endDateTime = event.getEndDateTime();
            snapshot.requiresRegistration = event.getRequiresRegistration();
            snapshot.isOpportunity = Boolean.TRUE.equals(event.getIsOpportunity())
                    || event.getOpportunity() != null;

            Opportunity opportunity = event.getOpportunity();

            if (opportunity != null) {
                snapshot.opportunityCompanyName = normalizeText(opportunity.getCompanyName());
                snapshot.opportunityRoleTitle = normalizeText(opportunity.getRoleTitle());
                snapshot.opportunityField = normalizeText(opportunity.getField());
                snapshot.opportunityIsPaid = opportunity.getIsPaid();
                snapshot.opportunityWorkMode = normalizeText(opportunity.getWorkMode());
                snapshot.opportunityApplicationDeadline = opportunity.getApplicationDeadline();
                snapshot.opportunityDurationWeeks = opportunity.getDurationWeeks();
                snapshot.opportunityApplicationUrl = normalizeText(opportunity.getApplicationUrl());
            }

            return snapshot;
        }

        private static String normalizeText(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }

            return value.trim();
        }
    }
}
