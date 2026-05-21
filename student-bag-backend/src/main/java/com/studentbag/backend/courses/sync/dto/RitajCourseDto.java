package com.studentbag.backend.courses.sync.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RitajCourseDto {
    private String externalId;
    private String code;
    private String nameArabic;
    private String nameEnglish;
    private String description;
    private String academicLevel;
    private Integer creditHours;

    private String facultyExternalId;
    private String departmentExternalId;

    private String facultyNameArabic;
    private String facultyNameEnglish;
    private String departmentNameArabic;
    private String departmentNameEnglish;

    private String programNameArabic;
    private String programNameEnglish;

    private List<RitajSectionDto> sections = new ArrayList<>();
}