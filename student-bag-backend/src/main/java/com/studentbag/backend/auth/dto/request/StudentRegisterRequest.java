package com.studentbag.backend.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentRegisterRequest extends BaseRegisterRequest {

    @NotNull

    private String academicLevel;
    private String schoolGrade;
    private String universityMajor;
}