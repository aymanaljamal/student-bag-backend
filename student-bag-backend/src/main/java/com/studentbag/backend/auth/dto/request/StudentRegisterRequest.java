package com.studentbag.backend.auth.dto.request;

import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentRegisterRequest extends BaseRegisterRequest {

    @NotNull
    private AcademicLevel academicLevel;

    private String universityMajor;
}