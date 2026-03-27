package com.studentbag.backend.institution.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionResponse {
    private Long id;
    private String name;
    private String type;
    private String country;
    private String city;
    private String website;
}