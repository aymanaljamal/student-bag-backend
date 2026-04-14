package com.studentbag.backend.events.dto.request;

import com.studentbag.backend.domain.enums.schedule.EventType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSearchRequestDTO {

    /**
     * General search over:
     * title, description, location, host, department,
     * companyName, roleTitle, field
     */
    private String query;

    private EventType eventType;

    private Boolean upcomingOnly;

    private Boolean requiresRegistration;

    private Boolean isOpportunity;

    private Boolean applicationOpenOnly;

    private Boolean paidOnly;

    private String department;

    private String host;

    private String workMode;

    private LocalDate startDateFrom;

    private LocalDate startDateTo;

    private LocalDate applicationDeadlineFrom;

    private LocalDate applicationDeadlineTo;

    /**
     * startDateTime, createdAt, title, applicationDeadline
     */
    private String sortBy;

    /**
     * asc / desc
     */
    private String sortDirection;
}