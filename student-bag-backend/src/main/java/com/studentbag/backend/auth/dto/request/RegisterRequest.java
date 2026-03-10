package com.studentbag.backend.auth.dto.request;

import com.studentbag.backend.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String phone;

    @NotNull
    private UserRole role;

    private Long institutionId;

    private String academicLevel;
    private String schoolGrade;
    private String universityMajor;
    private String department;
    private String adminScope;
    private String defaultRelationshipLabel;
}