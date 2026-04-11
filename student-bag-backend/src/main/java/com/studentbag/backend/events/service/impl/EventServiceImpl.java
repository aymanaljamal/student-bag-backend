package com.studentbag.backend.events.service.impl;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.events.dto.request.EventRegistrationRequestDTO;
import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.request.EventSearchRequestDTO;
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
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponseDTO createEvent(EventRequestDTO request, Long institutionId) {
        validateEventDates(request);

        Institution institution = getInstitutionById(institutionId);

        Event event = eventMapper.toEntity(request);
        event.setInstitution(institution);

        applyOpportunityDetails(event, request);

        Event savedEvent = eventRepository.save(event);

        log.info("Created event with id={} for institutionId={}", savedEvent.getId(), institutionId);
        return buildEventResponse(savedEvent, null);
    }

    @Override
    public EventResponseDTO updateEvent(Long eventId, EventRequestDTO request, Long institutionId) {
        validateEventDates(request);

        Event existingEvent = getEventByIdOrThrow(eventId);
        Institution institution = getInstitutionById(institutionId);

        eventMapper.updateEntity(existingEvent, request);
        existingEvent.setInstitution(institution);

        applyOpportunityDetails(existingEvent, request);

        Event savedEvent = eventRepository.save(existingEvent);

        log.info("Updated event with id={}", savedEvent.getId());
        return buildEventResponse(savedEvent, null);
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
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getStartDateTime, Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(event -> buildEventResponse(event, studentId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> searchEvents(Long studentId, EventSearchRequestDTO request) {
        return eventRepository.findAll().stream()
                .filter(event -> matchesSearch(event, request))
                .sorted(buildComparator(request))
                .map(event -> buildEventResponse(event, studentId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponseDTO> searchOpportunities(Long studentId, EventSearchRequestDTO request) {
        return eventRepository.findAll().stream()
                .filter(event -> Boolean.TRUE.equals(event.getIsOpportunity()))
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
    public void registerForEvent(Long eventId, Long studentId, EventRegistrationRequestDTO request) {
        Event event = getEventByIdOrThrow(eventId);
        Student student = getStudentById(studentId);

        validateRegistration(event, studentId);

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .student(student)
                .build();

        registration.register();
        registrationRepository.save(registration);

        log.info("Student {} registered for event {}", studentId, eventId);
    }

    @Override
    public void cancelRegistration(Long eventId, Long studentId) {
        EventRegistration registration = registrationRepository.findByEventIdAndStudentId(eventId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for event id: " + eventId + " and student id: " + studentId
                ));

        registration.cancel();
        registrationRepository.save(registration);

        log.info("Student {} cancelled registration for event {}", studentId, eventId);
    }

    @Override
    public void syncWithUniversityAPI(Long institutionId) {
        log.info("Sync with university API requested for institutionId={}", institutionId);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Institution getInstitutionById(Long institutionId) {
        return institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with id: " + institutionId));
    }

    private Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
    }

    private Event getEventByIdOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    private void validateEventDates(EventRequestDTO request) {
        if (request.getStartDateTime() != null
                && request.getEndDateTime() != null
                && request.getEndDateTime().isBefore(request.getStartDateTime())) {
            throw new IllegalArgumentException("End date/time must be after start date/time");
        }
    }

    private void applyOpportunityDetails(Event event, EventRequestDTO request) {
        if (Boolean.TRUE.equals(request.getIsOpportunity()) && request.getOpportunityDetails() != null) {
            event.setOpportunity(eventMapper.toOpportunityEntity(request.getOpportunityDetails(), event));
            return;
        }

        event.setOpportunity(null);
    }

    private EventResponseDTO buildEventResponse(Event event, Long studentId) {
        EventResponseDTO response = eventMapper.toResponseDTO(event, studentId);

        if (studentId == null) {
            return response;
        }

        registrationRepository.findByEventIdAndStudentId(event.getId(), studentId)
                .ifPresent(registration -> eventMapper.applyRegistrationInfo(response, registration));

        return response;
    }

    private void validateRegistration(Event event, Long studentId) {
        if (Boolean.FALSE.equals(event.getRequiresRegistration())) {
            throw new IllegalStateException("This event does not require registration");
        }

        if (!event.hasCapacity()) {
            throw new IllegalStateException("Event is already at full capacity");
        }

        if (registrationRepository.findByEventIdAndStudentId(event.getId(), studentId).isPresent()) {
            throw new IllegalStateException("Student is already registered for this event");
        }
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
                        || (opportunity != null && (
                        containsIgnoreCase(opportunity.getCompanyName(), request.getQuery())
                                || containsIgnoreCase(opportunity.getRoleTitle(), request.getQuery())
                                || containsIgnoreCase(opportunity.getField(), request.getQuery())
                ));

        boolean eventTypeMatches =
                request.getEventType() == null || request.getEventType() == event.getEventType();

        boolean upcomingMatches =
                request.getUpcomingOnly() == null
                        || !request.getUpcomingOnly()
                        || event.isUpcoming();

        boolean registrationMatches =
                request.getRequiresRegistration() == null
                        || Objects.equals(event.getRequiresRegistration(), request.getRequiresRegistration());

        boolean opportunityMatches =
                request.getIsOpportunity() == null
                        || Objects.equals(event.getIsOpportunity(), request.getIsOpportunity());

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
            return request.getStartDateFrom() == null && request.getStartDateTo() == null;
        }

        LocalDate startDate = event.getStartDateTime().toLocalDate();

        boolean fromMatches =
                request.getStartDateFrom() == null || !startDate.isBefore(request.getStartDateFrom());

        boolean toMatches =
                request.getStartDateTo() == null || !startDate.isAfter(request.getStartDateTo());

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

        return appOpenMatches && paidMatches && workModeMatches && deadlineMatches;
    }

    private boolean matchesApplicationDeadlineRange(Opportunity opportunity, EventSearchRequestDTO request) {
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

        String normalizedSortBy = isBlank(sortBy) ? "startDateTime" : sortBy.trim().toLowerCase();

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
                    event -> event.getOpportunity() != null ? event.getOpportunity().getApplicationDeadline() : null,
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

    private boolean containsIgnoreCase(String value, String query) {
        return value != null
                && query != null
                && value.toLowerCase().contains(query.trim().toLowerCase());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}