package com.studentbag.backend.grades.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatIfOptionDTO {

    private String optionCode;
    private String title;
    private String description;

    private BigDecimal projectedAverage;
    private boolean possible;

    @Builder.Default
    private List<WhatIfRequiredCourseDTO> courses = new ArrayList<>();
}
