package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAiContext {

    private Long id;

    private String title;
    private String description;

    private String resourceType;
    private String category;

    private String courseCode;
    private String courseName;

    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;

    private Boolean hasFile;
    private Boolean hasExternalLink;

    private String folderName;

    private Long linkedNoteId;
    private Long linkedTaskId;
}