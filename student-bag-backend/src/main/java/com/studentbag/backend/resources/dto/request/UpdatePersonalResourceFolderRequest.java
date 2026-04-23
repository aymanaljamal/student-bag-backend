package com.studentbag.backend.resources.dto.request;

import lombok.Data;

@Data
public class UpdatePersonalResourceFolderRequest {
    private String name;
    private String description;
    private Long parentFolderId;
    private Boolean showLinkedNotes;
    private Boolean showLinkedTasks;
    private Boolean isArchived;
}