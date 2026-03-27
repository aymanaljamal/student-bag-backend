package com.studentbag.backend.resources.dto.response;

import com.studentbag.backend.domain.enums.VisibilityScope;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminResourceResponse {

    private Long id;
    private Long learningObjectId;
    private Long institutionId;
    private Long termId;
    private Long courseId;
    private String gradeOrLevel;
    private VisibilityScope visibilityScope;
    private Integer version;
    private Boolean isApproved;
    private Long approvedByAdminId;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}