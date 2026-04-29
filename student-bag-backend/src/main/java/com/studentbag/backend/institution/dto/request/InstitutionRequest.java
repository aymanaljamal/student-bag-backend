package com.studentbag.backend.institution.dto.request;

import com.studentbag.backend.domain.enums.institution.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionRequest {

    @NotBlank(message = "Institution name is required")
    private String name;

    @NotNull(message = "Institution type is required")
    private InstitutionType type;

    private String country;
    private String city;
    private String website;
    private Boolean active;
}