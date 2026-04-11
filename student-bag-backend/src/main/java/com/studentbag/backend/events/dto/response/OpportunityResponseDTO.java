package com.studentbag.backend.events.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityResponseDTO {

    private String companyName;
    private String roleTitle;
    private String field;
    private Boolean isPaid;
    private String workMode;
    private LocalDate applicationDeadline;
    private String applicationUrl;
    private Integer durationWeeks;
}