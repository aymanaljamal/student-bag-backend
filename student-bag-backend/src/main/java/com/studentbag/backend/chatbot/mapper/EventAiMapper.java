package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.chatbot.dto.context.EventAiContext;
import com.studentbag.backend.events.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventAiMapper {

    public EventAiContext toContext(Event event) {
        if (event == null) return null;

        var opportunity = event.getOpportunity();

        return EventAiContext.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType() != null ? event.getEventType().name() : null)
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .location(event.getLocation())
                .department(event.getDepartment())
                .host(event.getHost())
                .requiresRegistration(event.getRequiresRegistration())
                .isOpportunity(event.getIsOpportunity())

                .companyName(opportunity != null ? opportunity.getCompanyName() : null)
                .roleTitle(opportunity != null ? opportunity.getRoleTitle() : null)
                .field(opportunity != null ? opportunity.getField() : null)
                .isPaid(opportunity != null ? opportunity.getIsPaid() : null)
                .workMode(opportunity != null ? opportunity.getWorkMode() : null)
                .applicationDeadline(opportunity != null ? opportunity.getApplicationDeadline() : null)
                .durationWeeks(opportunity != null ? opportunity.getDurationWeeks() : null)
                .build();
    }
}