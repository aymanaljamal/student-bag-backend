package com.studentbag.backend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyResetCodeRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String code;
}