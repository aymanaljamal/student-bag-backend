package com.studentbag.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstructorRegisterRequest extends BaseRegisterRequest {

    @NotNull
    private Long institutionId;

    @NotNull
    private Long departmentId;

    @NotBlank
    private String fullNameArabic;

    private String fullNameEnglish;
}