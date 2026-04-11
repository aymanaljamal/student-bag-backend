package com.studentbag.backend.tasks.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCourseSummaryResponse {

    private Long id;
    private String code;
    private String nameArabic;
    private String nameEnglish;
}