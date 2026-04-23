package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PersonalResourceFolderResponse {
    private Long id;
    private String name;
    private String description;

    private Long studentId;
    private Long parentFolderId;

    private Long courseId;
    private String courseCode;
    private String courseNameArabic;
    private String courseNameEnglish;

    private Boolean isRoot;
    private Boolean isSystemGenerated;
    private Boolean isDeleted;
    private Boolean isArchived;
    private Boolean showLinkedNotes;
    private Boolean showLinkedTasks;

    private Integer childFolderCount;
    private Integer itemCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}