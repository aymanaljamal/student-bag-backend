package com.studentbag.backend.events.mapper;

import com.studentbag.backend.domain.enums.EventStatus;
import com.studentbag.backend.domain.enums.courses.RegistrationStatus;
import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityDetailsDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;
import com.studentbag.backend.events.entity.Event;
import com.studentbag.backend.events.entity.EventRegistration;
import com.studentbag.backend.events.entity.Opportunity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventMapper {

    public Event toEntity(EventRequestDTO request) {
        if (request == null) {
            return null;
        }

        return Event.builder()
                .title(clean(request.getTitle()))
                .description(clean(request.getDescription()))
                .eventType(request.getEventType())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .location(clean(request.getLocation()))
                .department(clean(request.getDepartment()))
                .host(clean(request.getHost()))
                .imageUrl(clean(request.getImageUrl()))
                .requiresRegistration(Boolean.TRUE.equals(request.getRequiresRegistration()))
                .maxParticipants(request.getMaxParticipants())
                .isOpportunity(Boolean.TRUE.equals(request.getIsOpportunity()))
                .status(EventStatus.ACTIVE)
                .deletedAt(null)
                .build();
    }

    public void updateEntity(Event event, EventRequestDTO request) {
        if (event == null || request == null) {
            return;
        }

        event.setTitle(clean(request.getTitle()));
        event.setDescription(clean(request.getDescription()));
        event.setEventType(request.getEventType());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setLocation(clean(request.getLocation()));
        event.setDepartment(clean(request.getDepartment()));
        event.setHost(clean(request.getHost()));
        event.setImageUrl(clean(request.getImageUrl()));
        event.setRequiresRegistration(Boolean.TRUE.equals(request.getRequiresRegistration()));
        event.setMaxParticipants(request.getMaxParticipants());
        event.setIsOpportunity(Boolean.TRUE.equals(request.getIsOpportunity()));

        /*
         * لا نعدل status أو deletedAt من شاشة التعديل العادية.
         * تغيير الحالة يكون فقط من finish/cancel/delete/reopen.
         */
    }

    public EventResponseDTO toResponseDTO(Event event, Long studentId) {
        if (event == null) {
            return null;
        }

        EventStatus status = resolveStatus(event);

        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .status(status)
                .isEnded(isEnded(event, status))
                .isCancelled(status == EventStatus.CANCELLED)
                .isDeleted(status == EventStatus.DELETED)
                .deletedAt(event.getDeletedAt())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .location(event.getLocation())
                .department(event.getDepartment())
                .host(event.getHost())
                .imageUrl(event.getImageUrl())
                .createdByUserId(
                        event.getCreatedByUser() != null
                                ? event.getCreatedByUser().getId()
                                : null
                )
                .createdByFullName(
                        event.getCreatedByUser() != null
                                ? event.getCreatedByUser().getFullName()
                                : null
                )
                .createdByRole(
                        event.getCreatedByUser() != null
                                && event.getCreatedByUser().getRole() != null
                                ? event.getCreatedByUser().getRole().name()
                                : null
                )
                .isOpportunity(Boolean.TRUE.equals(event.getIsOpportunity()) || event.getOpportunity() != null)
                .requiresRegistration(Boolean.TRUE.equals(event.getRequiresRegistration()))
                .availableSlots(calculateAvailableSlots(event))
                .isUserRegistered(false)
                .registrationStatus(null)
                .opportunityInfo(toOpportunityResponseDTO(event.getOpportunity()))
                .build();
    }

    public OpportunityResponseDTO toOpportunityResponseDTO(Opportunity opportunity) {
        if (opportunity == null) {
            return null;
        }

        return OpportunityResponseDTO.builder()
                .companyName(opportunity.getCompanyName())
                .roleTitle(opportunity.getRoleTitle())
                .field(opportunity.getField())
                .isPaid(Boolean.TRUE.equals(opportunity.getIsPaid()))
                .workMode(opportunity.getWorkMode())
                .applicationDeadline(opportunity.getApplicationDeadline())
                .applicationUrl(opportunity.getApplicationUrl())
                .durationWeeks(opportunity.getDurationWeeks())
                .build();
    }

    public Opportunity toOpportunityEntity(OpportunityDetailsDTO dto, Event event) {
        if (dto == null) {
            return null;
        }

        return Opportunity.builder()
                .event(event)
                .companyName(clean(dto.getCompanyName()))
                .roleTitle(clean(dto.getRoleTitle()))
                .field(clean(dto.getField()))
                .isPaid(Boolean.TRUE.equals(dto.getIsPaid()))
                .workMode(clean(dto.getWorkMode()))
                .applicationDeadline(dto.getApplicationDeadline())
                .durationWeeks(dto.getDurationWeeks())
                .applicationUrl(clean(dto.getApplicationUrl()))
                .build();
    }

    public void applyRegistrationInfo(EventResponseDTO dto, EventRegistration registration) {
        if (dto == null) {
            return;
        }

        if (registration == null) {
            dto.setIsUserRegistered(false);
            dto.setRegistrationStatus(null);
            return;
        }

        boolean activeRegistration =
                registration.getStatus() == RegistrationStatus.REGISTERED
                        || registration.getStatus() == RegistrationStatus.CHECKED_IN;

        dto.setIsUserRegistered(activeRegistration);
        dto.setRegistrationStatus(
                registration.getStatus() != null
                        ? registration.getStatus().name()
                        : null
        );
    }

    public Integer calculateAvailableSlots(Event event) {
        if (event == null || event.getMaxParticipants() == null) {
            return null;
        }

        long activeRegistrations = 0;

        if (event.getRegistrations() != null) {
            activeRegistrations = event.getRegistrations()
                    .stream()
                    .filter(registration ->
                            registration.getStatus() == RegistrationStatus.REGISTERED
                                    || registration.getStatus() == RegistrationStatus.CHECKED_IN
                    )
                    .count();
        }

        return Math.max(0, event.getMaxParticipants() - (int) activeRegistrations);
    }

    private EventStatus resolveStatus(Event event) {
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

        if (event.getEndDateTime() != null
                && !event.getEndDateTime().isAfter(LocalDateTime.now())) {
            return EventStatus.FINISHED;
        }

        return EventStatus.ACTIVE;
    }

    private boolean isEnded(Event event, EventStatus status) {
        if (event == null) {
            return true;
        }

        if (status == EventStatus.FINISHED
                || status == EventStatus.CANCELLED
                || status == EventStatus.DELETED) {
            return true;
        }

        return event.getEndDateTime() != null
                && !event.getEndDateTime().isAfter(LocalDateTime.now());
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
