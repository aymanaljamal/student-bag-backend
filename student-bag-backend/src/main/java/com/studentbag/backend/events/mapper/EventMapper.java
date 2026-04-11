package com.studentbag.backend.events.mapper;

import com.studentbag.backend.domain.enums.RegistrationStatus;

import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityDetailsDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;
import com.studentbag.backend.events.entity.Event;
import com.studentbag.backend.events.entity.EventRegistration;
import com.studentbag.backend.events.entity.Opportunity;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequestDTO request) {
        if (request == null) {
            return null;
        }

        return Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .location(request.getLocation())
                .department(request.getDepartment())
                .host(request.getHost())
                .imageUrl(request.getImageUrl())
                .requiresRegistration(Boolean.TRUE.equals(request.getRequiresRegistration()))
                .maxParticipants(request.getMaxParticipants())
                .isOpportunity(Boolean.TRUE.equals(request.getIsOpportunity()))
                .build();
    }

    public void updateEntity(Event event, EventRequestDTO request) {
        if (event == null || request == null) {
            return;
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setLocation(request.getLocation());
        event.setDepartment(request.getDepartment());
        event.setHost(request.getHost());
        event.setImageUrl(request.getImageUrl());
        event.setRequiresRegistration(Boolean.TRUE.equals(request.getRequiresRegistration()));
        event.setMaxParticipants(request.getMaxParticipants());
        event.setIsOpportunity(Boolean.TRUE.equals(request.getIsOpportunity()));
    }

    public EventResponseDTO toResponseDTO(Event event, Long studentId) {
        if (event == null) {
            return null;
        }

        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .location(event.getLocation())
                .department(event.getDepartment())
                .host(event.getHost())
                .imageUrl(event.getImageUrl())
                .isOpportunity(event.getIsOpportunity())
                .requiresRegistration(event.getRequiresRegistration())
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
                .isPaid(opportunity.getIsPaid())
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
                .companyName(dto.getCompanyName())
                .roleTitle(dto.getRoleTitle())
                .field(dto.getField())
                .isPaid(Boolean.TRUE.equals(dto.getIsPaid()))
                .workMode(dto.getWorkMode())
                .applicationDeadline(dto.getApplicationDeadline())
                .durationWeeks(dto.getDurationWeeks())
                .applicationUrl(dto.getApplicationUrl())
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

        dto.setIsUserRegistered(
                registration.getStatus() == RegistrationStatus.REGISTERED
                        || registration.getStatus() == RegistrationStatus.CHECKED_IN
        );
        dto.setRegistrationStatus(registration.getStatus().name());
    }

    public Integer calculateAvailableSlots(Event event) {
        if (event == null || event.getMaxParticipants() == null) {
            return null;
        }

        long activeRegistrations = 0;
        if (event.getRegistrations() != null) {
            activeRegistrations = event.getRegistrations().stream()
                    .filter(registration ->
                            registration.getStatus() == RegistrationStatus.REGISTERED
                                    || registration.getStatus() == RegistrationStatus.CHECKED_IN
                    )
                    .count();
        }

        return Math.max(0, event.getMaxParticipants() - (int) activeRegistrations);
    }
}