package com.studentbag.backend.student.dto.request;

import lombok.Data;

@Data
public class UpdateStudentProfileRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String languageCode;
    private String academicLevel;
    private String universityMajor;
    private Long institutionId;
}