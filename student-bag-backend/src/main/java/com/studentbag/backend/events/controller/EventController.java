package com.studentbag.backend.events.controller;

import com.studentbag.backend.events.dto.request.*;
import com.studentbag.backend.events.dto.response.EventRegistrationInfoDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;
import com.studentbag.backend.events.service.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(
        name = "University Events & Opportunities",
        description = "Endpoints for managing academic events, workshops, and professional opportunities"
)
public class EventController {

    private final EventService eventService;

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO request,
            @RequestParam Long institutionId,
            Authentication authentication
    ) {
        EventResponseDTO response = eventService.createEvent(
                request,
                institutionId,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PatchMapping("/{eventId}/reopen")
    public ResponseEntity<EventResponseDTO> reopenEvent(
            @PathVariable Long eventId,
            @RequestBody EventReopenRequestDTO request,
            Authentication authentication
    ) {
        EventResponseDTO response = eventService.reopenEvent(
                eventId,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestDTO request,
            @RequestParam Long institutionId,
            Authentication authentication
    ) {
        EventResponseDTO response = eventService.updateEvent(
                eventId,
                request,
                institutionId,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PatchMapping("/{eventId}/finish")
    public ResponseEntity<EventResponseDTO> finishEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                eventService.finishEvent(eventId, authentication.getName())
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventResponseDTO> cancelEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                eventService.cancelEvent(eventId, authentication.getName())
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId
    ) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.getAllEvents(studentId));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEventById(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.getEventById(eventId, studentId));
    }

    @PostMapping("/search")
    public ResponseEntity<List<EventResponseDTO>> searchEvents(
            @Valid @RequestBody EventSearchRequestDTO request,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.searchEvents(studentId, request));
    }

    @PostMapping("/opportunities/search")
    public ResponseEntity<List<OpportunityResponseDTO>> searchOpportunities(
            @Valid @RequestBody EventSearchRequestDTO request,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.searchOpportunities(studentId, request));
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<Void> registerForEvent(
            @PathVariable Long eventId,
            @RequestParam Long studentId
    ) {
        eventService.registerForEvent(eventId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{eventId}/register/details")
    public ResponseEntity<Void> registerForEventWithBody(
            @PathVariable Long eventId,
            @RequestParam Long studentId,
            @Valid @RequestBody EventRegistrationRequestDTO request
    ) {
        eventService.registerForEvent(eventId, studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{eventId}/register")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable Long eventId,
            @RequestParam Long studentId
    ) {
        eventService.cancelRegistration(eventId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<List<EventRegistrationInfoDTO>> getEventRegistrations(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                eventService.getEventRegistrations(eventId, authentication.getName())
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @GetMapping("/{eventId}/registrations/count")
    public ResponseEntity<Long> getEventRegistrationCount(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                eventService.getEventRegistrationCount(eventId, authentication.getName())
        );
    }
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PostMapping("/{eventId}/registrations/notify")
    public ResponseEntity<Void> notifyEventRegistrants(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRegistrantsNotificationRequestDTO request,
            Authentication authentication
    ) {
        eventService.notifyEventRegistrants(
                eventId,
                request,
                authentication.getName()
        );

        return ResponseEntity.accepted().build();
    }
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PostMapping("/sync")
    public ResponseEntity<Void> syncEvents(
            @RequestParam Long institutionId
    ) {
        eventService.syncWithUniversityAPI(institutionId);
        return ResponseEntity.accepted().build();
    }
}