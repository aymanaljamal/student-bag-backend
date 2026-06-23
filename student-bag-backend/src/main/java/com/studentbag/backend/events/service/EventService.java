package com.studentbag.backend.events.service;

import com.studentbag.backend.events.dto.request.*;
import com.studentbag.backend.events.dto.response.EventRegistrationInfoDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;

import java.util.List;

public interface EventService {

    EventResponseDTO createEvent(
            EventRequestDTO request,
            Long institutionId,
            String currentUserEmail
    );

    EventResponseDTO updateEvent(
            Long eventId,
            EventRequestDTO request,
            Long institutionId,
            String currentUserEmail
    );
    EventResponseDTO reopenEvent(
            Long eventId,
            EventReopenRequestDTO request,
            String currentUserEmail
    );
    EventResponseDTO finishEvent(Long eventId, String currentUserEmail);

    EventResponseDTO cancelEvent(Long eventId, String currentUserEmail);

    void deleteEvent(Long eventId);

    EventResponseDTO getEventById(Long eventId, Long studentId);

    List<EventResponseDTO> getAllEvents(Long studentId);

    List<EventResponseDTO> searchEvents(Long studentId, EventSearchRequestDTO request);

    List<OpportunityResponseDTO> searchOpportunities(
            Long studentId,
            EventSearchRequestDTO request
    );

    void registerForEvent(Long eventId, Long studentId);

    void registerForEvent(
            Long eventId,
            Long studentId,
            EventRegistrationRequestDTO request
    );

    void cancelRegistration(Long eventId, Long studentId);

    List<EventRegistrationInfoDTO> getEventRegistrations(
            Long eventId,
            String currentUserEmail
    );

    long getEventRegistrationCount(
            Long eventId,
            String currentUserEmail
    );

    void syncWithUniversityAPI(Long institutionId);
    void notifyEventRegistrants(
            Long eventId,
            EventRegistrantsNotificationRequestDTO request,
            String currentUserEmail
    );


}