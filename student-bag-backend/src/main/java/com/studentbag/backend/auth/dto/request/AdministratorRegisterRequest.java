package com.studentbag.backend.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdministratorRegisterRequest extends BaseRegisterRequest {

    @NotNull
    private Long institutionId;

    private String adminScope;
}