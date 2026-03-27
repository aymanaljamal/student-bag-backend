package com.studentbag.backend.resources.dto.request;

import com.studentbag.backend.domain.enums.VisibilityScope;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResourceRequest {

    @NotNull
    private Long learningObjectId;

    @NotNull
    private Long institutionId;

    private Long termId;

    private Long courseId;

    private String gradeOrLevel;

    @NotNull
    private VisibilityScope visibilityScope;

    private Integer version = 1;

    private Boolean isApproved = false;

    private Long approvedByAdminId;
}