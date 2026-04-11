package com.studentbag.backend.events.dto.response;

import com.studentbag.backend.domain.enums.EventType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Boolean requiresRegistration;

    /**
     * Computed fields
     */
    private Boolean isUserRegistered;
    private String registrationStatus;
    private Integer availableSlots;

    private OpportunityResponseDTO opportunityInfo;
}