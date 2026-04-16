package com.studentbag.backend.events.controller;

import com.studentbag.backend.events.dto.request.EventRegistrationRequestDTO;
import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.request.EventSearchRequestDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.dto.response.OpportunityResponseDTO;
import com.studentbag.backend.events.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // -------------------------------------------------------------------------
    // Event Management
    // -------------------------------------------------------------------------

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PostMapping
    @Operation(
            summary = "Create event",
            description = "Create a new academic event or opportunity for a specific institution."
    )
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO request,
            @Parameter(description = "Institution ID", example = "1")
            @RequestParam Long institutionId
    ) {
        EventResponseDTO response = eventService.createEvent(request, institutionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PutMapping("/{eventId}")
    @Operation(
            summary = "Update event",
            description = "Update an existing event or opportunity."
    )
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "Event ID", example = "10")
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestDTO request,
            @Parameter(description = "Institution ID", example = "1")
            @RequestParam Long institutionId
    ) {
        EventResponseDTO response = eventService.updateEvent(eventId, request, institutionId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PatchMapping("/{eventId}/finish")
    @Operation(
            summary = "Finish event",
            description = "Mark an event as ended immediately."
    )
    public ResponseEntity<EventResponseDTO> finishEvent(
            @Parameter(description = "Event ID", example = "10")
            @PathVariable Long eventId
    ) {
        EventResponseDTO response = eventService.finishEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @DeleteMapping("/{eventId}")
    @Operation(
            summary = "Delete event",
            description = "Delete an event permanently."
    )
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "Event ID", example = "10")
            @PathVariable Long eventId
    ) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Get all events",
            description = "Return all events. If studentId is provided, registration status is included."
    )
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @Parameter(description = "Student ID", example = "5")
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.getAllEvents(studentId));
    }

    @GetMapping("/{eventId}")
    @Operation(
            summary = "Get event by ID",
            description = "Return full details of a specific event."
    )
    public ResponseEntity<EventResponseDTO> getEventById(
            @Parameter(description = "Event ID", example = "10")
            @PathVariable Long eventId,
            @Parameter(description = "Student ID", example = "5")
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.getEventById(eventId, studentId));
    }

    // -------------------------------------------------------------------------
    // Event Search
    // -------------------------------------------------------------------------

    @PostMapping("/search")
    @Operation(
            summary = "Search events",
            description = "Search events using filters such as query, event type, upcoming status, registration requirement, department, host, date range, and sorting."
    )
    public ResponseEntity<List<EventResponseDTO>> searchEvents(
            @Valid @RequestBody EventSearchRequestDTO request,
            @Parameter(description = "Student ID", example = "5")
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.searchEvents(studentId, request));
    }

    @PostMapping("/opportunities/search")
    @Operation(
            summary = "Search opportunities",
            description = "Search professional opportunities using filters such as query, paid status, work mode, application deadline, and opportunity-specific criteria."
    )
    public ResponseEntity<List<OpportunityResponseDTO>> searchOpportunities(
            @Valid @RequestBody EventSearchRequestDTO request,
            @Parameter(description = "Student ID", example = "5")
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(eventService.searchOpportunities(studentId, request));
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    @PostMapping("/{eventId}/register")
    @Operation(
            summary = "Register for event",
            description = "Register a student for an event with capacity and duplicate-registration checks."
    )
    public ResponseEntity<Void> registerForEvent(
            @Parameter(description = "Event ID", example = "10")
            @PathVariable Long eventId,
            @Parameter(description = "Student ID", example = "5")
            @RequestParam Long studentId
    ) {
        eventService.registerForEvent(eventId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{eventId}/register/details")
    @Operation(
            summary = "Register for event with request body",
            description = "Register a student for an event using an optional registration request body."
    )
    public ResponseEntity<Void> registerForEventWithBody(
            @Parameter(description = "Event ID", example = "10")
            @PathVariable Long eventId,
            @Parameter(description = "Student ID", example = "5")
            @RequestParam Long studentId,
            @Valid @RequestBody EventRegistrationRequestDTO request
    ) {
        eventService.registerForEvent(eventId, studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{eventId}/register")
    @Operation(
            summary = "Cancel registration",
            description = "Cancel a student's registration for an event."
    )
    public ResponseEntity<Void> cancelRegistration(
            @Parameter(description = "Event ID", example = "10")
            @PathVariable Long eventId,
            @Parameter(description = "Student ID", example = "5")
            @RequestParam Long studentId
    ) {
        eventService.cancelRegistration(eventId, studentId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // External Sync
    // -------------------------------------------------------------------------

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','INSTRUCTOR')")
    @PostMapping("/sync")
    @Operation(
            summary = "Sync events from university API",
            description = "Trigger synchronization with an external university events source."
    )
    public ResponseEntity<Void> syncEvents(
            @Parameter(description = "Institution ID", example = "1")
            @RequestParam Long institutionId
    ) {
        eventService.syncWithUniversityAPI(institutionId);
        return ResponseEntity.accepted().build();
    }
}