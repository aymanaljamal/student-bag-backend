package com.studentbag.backend.events.dto.request;

import com.studentbag.backend.domain.enums.schedule.EventType;
import com.studentbag.backend.events.dto.response.OpportunityDetailsDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Start date/time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date/time is required")
    private LocalDateTime endDateTime;

    private String location;

    private String department;

    private String host;

    private String imageUrl;

    @Builder.Default
    private Boolean requiresRegistration = false;

    private Integer maxParticipants;

    @Builder.Default
    private Boolean isOpportunity = false;

    @Valid
    private OpportunityDetailsDTO opportunityDetails;
}