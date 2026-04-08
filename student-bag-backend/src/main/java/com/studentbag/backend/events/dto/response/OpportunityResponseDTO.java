package com.studentbag.backend.events.dto.response;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
public class OpportunityResponseDTO {
    private String companyName;
    private String roleTitle;
    private String field;
    private Boolean isPaid;
    private String workMode; // Remote, On-site, Hybrid
    private LocalDate applicationDeadline;
    private String applicationUrl;
    private Integer durationWeeks;
}