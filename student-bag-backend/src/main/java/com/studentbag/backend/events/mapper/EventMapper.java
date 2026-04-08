package com.studentbag.backend.events.mapper;
import com.studentbag.backend.events.dto.OpportunityDetailsDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;
import com.studentbag.backend.events.entity.Event;
import com.studentbag.backend.events.entity.Opportunity;
import org.springframework.stereotype.Component;

/**
 * Mapper component to handle conversions between Event/Opportunity entities
 * and their respective Data Transfer Objects (DTOs).
 */
@Component
public class EventMapper {

    /**
     * Converts an Event entity to a Response DTO for the student view.
     */
    public EventResponseDTO toResponseDTO(Event event, Long studentId) {
        if (event == null) return null;

        EventResponseDTO dto = EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .location(event.getLocation())
                .imageUrl(event.getImageUrl())
                .department(event.getDepartment())
                .host(event.getHost())
                .isOpportunity(event.getIsOpportunity())
                .requiresRegistration(event.getRequiresRegistration())
                .build();

        // FR-9.4: Calculate available slots dynamically
        if (event.getMaxParticipants() != null) {
            int currentRegistrations = (event.getRegistrations() != null) ? event.getRegistrations().size() : 0;
            dto.setAvailableSlots(Math.max(0, event.getMaxParticipants() - currentRegistrations));
        }

        // FR-9.6: If it's an opportunity, map nested details
        if (Boolean.TRUE.equals(event.getIsOpportunity()) && event.getOpportunity() != null) {
            dto.setOpportunityInfo(toOpportunityResponse(event.getOpportunity()));
        }

        return dto;
    }

    /**
     * Maps the specific details of a professional opportunity to its DTO.
     */
    public OpportunityResponseDTO toOpportunityResponse(Opportunity opportunity) {
        if (opportunity == null) return null;

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

    /**
     * Converts the creation request DTO into an Opportunity entity.
     */
    public Opportunity toOpportunityEntity(OpportunityDetailsDTO dto, Event event) {
        if (dto == null) return null;

        return Opportunity.builder()
                .event(event)
                .companyName(dto.getCompanyName())
                .roleTitle(dto.getRoleTitle())
                .field(dto.getField())
                .isPaid(dto.getIsPaid())
                .workMode(dto.getWorkMode())
                .applicationDeadline(dto.getApplicationDeadline())
                .applicationUrl(dto.getApplicationUrl())
                .durationWeeks(dto.getDurationWeeks())
                .build();
    }
}