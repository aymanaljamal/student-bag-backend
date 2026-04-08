package com.studentbag.backend.events.service;

import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import java.util.List;

public interface EventService {
    EventResponseDTO createEvent(EventRequestDTO request, Long institutionId);
    List<EventResponseDTO> getAllEvents(Long studentId);
    EventResponseDTO getEventById(Long eventId, Long studentId);
    void registerForEvent(Long eventId, Long studentId);
    void cancelRegistration(Long eventId, Long studentId);
    void syncWithUniversityAPI(Long institutionId); // FR-9.2 & FR-9.7
}