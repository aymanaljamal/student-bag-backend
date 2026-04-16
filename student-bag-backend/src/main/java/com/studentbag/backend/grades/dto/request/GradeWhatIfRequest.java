package com.studentbag.backend.grades.dto.request;

import com.studentbag.backend.domain.enums.grades.WhatIfScope;
import com.studentbag.backend.domain.enums.grades.WhatIfTargetType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeWhatIfRequest {

    private BigDecimal targetValue; // مثل 81 أو 3.2
    private WhatIfTargetType targetType; // PERCENTAGE أو GPA
    private WhatIfScope scope; // SEMESTER أو CUMULATIVE

    // فقط للتراكمي
    private BigDecimal currentCumulativeValue;
    private BigDecimal completedCredits;
}