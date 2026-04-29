package com.studentbag.backend.institution.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionResponse {

    private Long id;
    private String name;
    private String type;
    private String country;
    private String city;
    private String website;
    private Boolean active;
}