package com.studentbag.backend.instructor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstructorUpdateProfileRequest {

    @NotBlank
    private String fullNameArabic;

    private String fullNameEnglish;

    private String phone;

    private String avatarUrl;

    private String languageCode;

    @NotNull
    private Long departmentId;
}