package com.studentbag.backend.events.controller;
import com.studentbag.backend.events.dto.request.EventRequestDTO;
import com.studentbag.backend.events.dto.response.EventResponseDTO;
import com.studentbag.backend.events.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * REST Controller for managing University Events and Professional Opportunities.
 * Provides endpoints for event discovery, detailed views, and student registration.
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "University Events & Opportunities", description = "Endpoints for managing academic events, workshops, and career opportunities")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create a new event or opportunity",
            description = "Allows authorized staff to create academic events or job opportunities. Required for FR-9.2.")
    @ApiResponse(responseCode = "201", description = "Event successfully created")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO request,
            @RequestParam Long institutionId) {
        return new ResponseEntity<>(eventService.createEvent(request, institutionId), HttpStatus.CREATED);
    }
    @GetMapping
    @Operation(summary = "Get all events",
            description = "Fetch all events. If studentId is provided, registration status will be included.")
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(eventService.getAllEvents(studentId));
    }
    @GetMapping("/{eventId}")
    @Operation(summary = "Get detailed event information",
            description = "Returns full details of an event, including opportunity data if applicable.")
    public ResponseEntity<EventResponseDTO> getEventById(
            @PathVariable Long eventId,
            @RequestParam Long studentId) {
        return ResponseEntity.ok(eventService.getEventById(eventId, studentId));
    }
    @PostMapping("/{eventId}/register")
    @Operation(summary = "Student registration for an event",
            description = "Registers the student and triggers capacity checks. Links to Smart Schedule (FR-9.4, FR-9.5).")
    @ApiResponse(responseCode = "201", description = "Successfully registered")
    public ResponseEntity<Void> register(@PathVariable Long eventId, @RequestParam Long studentId) {
        eventService.registerForEvent(eventId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel event registration",
            description = "Removes the student from the participant list. Required for FR-9.8.")
    @ApiResponse(responseCode = "204", description = "Registration successfully cancelled")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long eventId, @RequestParam Long studentId) {
        eventService.cancelRegistration(eventId, studentId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/sync")
    @Operation(summary = "University System Synchronization",
            description = "Manually triggers a sync with external university APIs. Supports FR-9.2 and FR-9.7.")
    @ApiResponse(responseCode = "202", description = "Sync request accepted and processing")
    public ResponseEntity<Void> syncEvents(@RequestParam Long institutionId) {
        eventService.syncWithUniversityAPI(institutionId);
        return ResponseEntity.accepted().build();
    }
}