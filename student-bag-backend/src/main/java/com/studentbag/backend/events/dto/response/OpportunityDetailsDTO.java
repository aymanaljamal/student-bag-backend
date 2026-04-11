package com.studentbag.backend.events.dto.response;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityDetailsDTO {

    @NotBlank(message = "Company or organization name is required")
    private String companyName;

    @NotBlank(message = "Role or position title is required")
    private String roleTitle;

    private String field;

    @Builder.Default
    private Boolean isPaid = false;

    private String workMode;

    @Future(message = "Application deadline must be in the future")
    private LocalDate applicationDeadline;

    private Integer durationWeeks;

    private String applicationUrl;
}