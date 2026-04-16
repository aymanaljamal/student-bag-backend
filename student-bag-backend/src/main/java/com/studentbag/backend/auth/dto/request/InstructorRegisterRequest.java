package com.studentbag.backend.auth.dto.request;

import com.studentbag.backend.courses.entity.Department;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstructorRegisterRequest extends BaseRegisterRequest {

    @NotNull
    private Long institutionId;

    private Department department;
}