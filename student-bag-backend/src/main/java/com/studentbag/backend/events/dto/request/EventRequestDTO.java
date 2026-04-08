package com.studentbag.backend.events.dto.request;

import com.studentbag.backend.domain.enums.EventType;
import com.studentbag.backend.events.dto.OpportunityDetailsDTO;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    private String imageUrl;
    @NotNull(message = "Start date/time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date/time is required")
    private LocalDateTime endDateTime;

    private String location;
    private String department;
    private String host;

    private Boolean requiresRegistration;
    private Integer maxParticipants;

    // FR-9.6: إذا كان الحدث "Opportunity" بنعبي هاي البيانات
    private Boolean isOpportunity;
    private OpportunityDetailsDTO opportunityDetails;
}