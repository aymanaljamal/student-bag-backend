package com.studentbag.backend.instructor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorProfileResponse {

    private Long id;
    private String externalId;

    private String fullNameArabic;
    private String fullNameEnglish;

    private Long departmentId;
    private String departmentNameArabic;
    private String departmentNameEnglish;

    private Long institutionId;
    private String institutionName;

    private String email;
    private String phone;
    private String avatarUrl;

    private Boolean accountConfirmed;
    private Boolean active;
}