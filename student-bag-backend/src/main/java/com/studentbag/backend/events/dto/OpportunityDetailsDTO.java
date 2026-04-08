package com.studentbag.backend.events.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityDetailsDTO {

    @NotBlank(message = "Company or Organization name is required")
    private String companyName;

    @NotBlank(message = "Role or Position title is required")
    private String roleTitle;

    private String field; // e.g., Software Engineering, Marketing

    private Boolean isPaid;

    private String workMode; // e.g., Remote, On-site, Hybrid

    @Future(message = "Application deadline must be in the future")
    private LocalDate applicationDeadline;

    private Integer durationWeeks;

    private String applicationUrl;
}