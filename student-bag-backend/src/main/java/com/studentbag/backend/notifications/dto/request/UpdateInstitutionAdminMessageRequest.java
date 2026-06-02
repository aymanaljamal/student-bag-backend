package com.studentbag.backend.notifications.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInstitutionAdminMessageRequest {

    @NotBlank(message = "Subject is required.")
    @Size(max = 200, message = "Subject must not exceed 200 characters.")
    private String subject;

    @NotBlank(message = "Message body is required.")
    @Size(max = 5000, message = "Message body must not exceed 5000 characters.")
    private String body;
}