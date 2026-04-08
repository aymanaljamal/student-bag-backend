package com.studentbag.backend.events.dto.response;

import com.studentbag.backend.domain.enums.EventType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private EventType eventType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String department;
    private String host;
    private String imageUrl;
    private Boolean isOpportunity;
    // Status for the student (FR-9.8)
    private Boolean isUserRegistered;
    private String registrationStatus; // e.g., "REGISTERED", "CANCELLED"

    private Boolean requiresRegistration;
    private Integer availableSlots; // المحسوبة في المابير: max - current_registrations

    private OpportunityResponseDTO opportunityInfo;
}