package com.studentbag.backend.grades.dto.response;

import com.studentbag.backend.domain.enums.grades.GradeAdviceLevel;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeAdviceDTO {
    private GradeAdviceLevel level;
    private String title;
    private String message;
}