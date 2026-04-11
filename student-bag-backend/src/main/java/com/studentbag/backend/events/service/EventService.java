package com.studentbag.backend.events.service;

import com.studentbag.backend.events.dto.request.EventRegistrationRequestDTO;
import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.request.EventSearchRequestDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;

import java.util.List;

public interface EventService {

    EventResponseDTO createEvent(EventRequestDTO request, Long institutionId);

    EventResponseDTO updateEvent(Long eventId, EventRequestDTO request, Long institutionId);

    EventResponseDTO getEventById(Long eventId, Long studentId);

    List<EventResponseDTO> getAllEvents(Long studentId);

    List<EventResponseDTO> searchEvents(Long studentId, EventSearchRequestDTO request);

    List<OpportunityResponseDTO> searchOpportunities(Long studentId, EventSearchRequestDTO request);

    void registerForEvent(Long eventId, Long studentId);

    void registerForEvent(Long eventId, Long studentId, EventRegistrationRequestDTO request);

    void cancelRegistration(Long eventId, Long studentId);

    void syncWithUniversityAPI(Long institutionId);
}