package com.studentbag.backend.schedule.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseSectionDTO {
    private Long sectionId;
    private String courseCode;
    private String courseName;
    private String sectionNumber;
    private String instructorName;
    private List<ClassSessionDTO> sessions;
    
}