package com.studentbag.backend.events.service.impl;

import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.entity.Event;
import com.studentbag.backend.events.entity.EventRegistration;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing University Events and Opportunities.
 * Handles event creation, registration logic, and data enrichment for students.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO request, Long institutionId) {
        log.info("Creating new event: {} for institution ID: {}", request.getTitle(), institutionId);

        // Fetch institution to ensure the event is linked correctly (Required by Entity constraints)
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found with ID: " + institutionId));

        Event event = Event.builder()
                .institution(institution)
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .location(request.getLocation())
                .department(request.getDepartment())
                .host(request.getHost())
                .imageUrl(request.getImageUrl())
                .requiresRegistration(request.getRequiresRegistration())
                .maxParticipants(request.getMaxParticipants())
                .isOpportunity(request.getIsOpportunity())
                .build();

        // Handle Opportunity details if the event is a career/training opportunity
        if (Boolean.TRUE.equals(request.getIsOpportunity()) && request.getOpportunityDetails() != null) {
            event.setOpportunity(eventMapper.toOpportunityEntity(request.getOpportunityDetails(), event));
        }

        Event savedEvent = eventRepository.save(event);
        log.info("Event successfully created with ID: {}", savedEvent.getId());

        return eventMapper.toResponseDTO(savedEvent, null);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long eventId, Long studentId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        return enrichWithRegistrationStatus(event, studentId);
    }

    @Override
    @Transactional
    public void registerForEvent(Long eventId, Long studentId) {
        log.info("Registering student ID: {} for event ID: {}", studentId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Business Logic: Check capacity (FR-9.4)
        if (!event.hasCapacity()) {
            log.warn("Registration failed: Event {} is at full capacity", eventId);
            throw new RuntimeException("Event is already at full capacity!");
        }

        // Business Logic: Prevent duplicate registrations
        if (registrationRepository.findByEventIdAndStudentId(eventId, studentId).isPresent()) {
            throw new RuntimeException("Student is already registered for this event.");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .student(student)
                .build();

        registration.register();
        registrationRepository.save(registration);

        log.info("Successfully registered student {} for event {}", studentId, eventId);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long eventId, Long studentId) {
        log.info("Cancelling registration for student ID: {} on event ID: {}", studentId, eventId);

        EventRegistration reg = registrationRepository.findByEventIdAndStudentId(eventId, studentId)
                .orElseThrow(() -> new RuntimeException("Registration record not found for student " + studentId));

        reg.cancel();
        registrationRepository.save(reg);
    }

    @Override
    public void syncWithUniversityAPI(Long institutionId) {
        // FR-9.2 & FR-9.7: External API synchronization logic placeholder
        log.info("Initiating university API sync for institution {}", institutionId);
    }

    /**
     * Helper method to map an Event entity to a Response DTO and inject
     * the specific registration status for the requesting student.
     */
    private EventResponseDTO enrichWithRegistrationStatus(Event event, Long studentId) {
        EventResponseDTO dto = eventMapper.toResponseDTO(event, studentId);

        registrationRepository.findByEventIdAndStudentId(event.getId(), studentId)
                .ifPresent(reg -> {
                    dto.setIsUserRegistered(true);
                    dto.setRegistrationStatus(reg.getStatus().name());
                });

        return dto;
    }
    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents(Long studentId) {
        log.info("Fetching all events. Student context: {}", studentId != null ? studentId : "Guest");

        return eventRepository.findAll().stream()
                .map(event -> {
                    EventResponseDTO dto = eventMapper.toResponseDTO(event, studentId);

                    if (studentId != null) {
                        registrationRepository.findByEventIdAndStudentId(event.getId(), studentId)
                                .ifPresent(reg -> {
                                    dto.setIsUserRegistered(true);
                                    dto.setRegistrationStatus(reg.getStatus().name());
                                });
                    } else {
                        dto.setIsUserRegistered(false);
                        dto.setRegistrationStatus(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}