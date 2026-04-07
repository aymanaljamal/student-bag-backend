package com.studentbag.backend.courses.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseSectionSimpleDTO {

    private Long id;

    private String sectionNumber; // مثلا: 1 ، 2 ، A

    private String sectionType;   // LECTURE / LAB

}