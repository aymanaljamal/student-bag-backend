package com.studentbag.backend.courses.sync.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RitajCourseDto {
    private String externalId;
    private String code;

    /**
     * Comes from STUDENT_BAG_FINAL_DATA_normalized.json.
     * Used to avoid duplicate course-code collisions such as COMP438.
     */
    private String courseInternalId;

    /**
     * Runtime-only value prepared by the sync service.
     * For normal courses: storageCode = code.
     * For duplicated codes: storageCode = courseInternalId.
     */
    private String storageCode;

    private String nameArabic;
    private String nameEnglish;

    private String description;
    private String descriptionArabic;
    private String descriptionEnglish;

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

    private Boolean isGeneratedName;
    private Boolean isGeneratedDescription;
    private Boolean isLabCourse;

    private List<RitajSectionDto> sections = new ArrayList<>();
}
