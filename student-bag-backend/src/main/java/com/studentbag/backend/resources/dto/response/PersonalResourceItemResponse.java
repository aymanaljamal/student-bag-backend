package com.studentbag.backend.resources.dto.response;

import com.studentbag.backend.domain.enums.ContentFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PersonalResourceItemResponse {

    private Long id;
    private Long ownerStudentId;
    private Long folderId;
    private String title;
    private ContentFormat format;
    private Boolean isExamPreparation;
    private Boolean isImportant;
    private Long linkedAdminResourceId;
    private Long noteId;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}