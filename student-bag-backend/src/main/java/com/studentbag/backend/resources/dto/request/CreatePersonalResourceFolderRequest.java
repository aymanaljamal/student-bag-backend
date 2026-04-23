package com.studentbag.backend.resources.dto.request;

import lombok.Data;

@Data
public class CreatePersonalResourceFolderRequest {
    private String name;
    private String description;
    private Long parentFolderId;
    private Long courseId;
    private Boolean showLinkedNotes;
    private Boolean showLinkedTasks;
}