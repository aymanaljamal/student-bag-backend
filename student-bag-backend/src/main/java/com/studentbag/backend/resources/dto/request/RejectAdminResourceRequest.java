package com.studentbag.backend.resources.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectAdminResourceRequest {

    @NotBlank
    private String adminNotes;
}