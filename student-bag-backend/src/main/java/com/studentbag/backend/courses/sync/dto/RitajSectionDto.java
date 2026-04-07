package com.studentbag.backend.courses.sync.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RitajSectionDto {
    private String externalId;
    private String sectionNumber;
    private String sectionType;

    private String instructorNameArabic;
    private String instructorNameEnglish;

    private String courseCode;

    private Integer capacity;
    private Integer enrolled;

    private String parentLectureSectionNumber;

    private List<RitajClassSessionDto> sessions = new ArrayList<>();
}