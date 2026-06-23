package com.studentbag.backend.events.dto.response;

import com.studentbag.backend.domain.enums.EventStatus;
import com.studentbag.backend.domain.enums.schedule.EventType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private EventStatus status;
    private Boolean isEnded;
    private Boolean isCancelled;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private UUID createdByUserId;
    private String createdByFullName;
    private String createdByRole;
    private String location;
    private String department;
    private String host;
    private String imageUrl;
    private Boolean isOpportunity;
    private Boolean requiresRegistration;
    private Boolean isUserRegistered;
    private String registrationStatus;
    private Integer availableSlots;
    private OpportunityResponseDTO opportunityInfo;
}
