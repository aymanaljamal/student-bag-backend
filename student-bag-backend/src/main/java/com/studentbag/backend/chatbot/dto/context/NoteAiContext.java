package com.studentbag.backend.chatbot.dto.context;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteAiContext {

    private Long id;

    private String title;
    private String contentText;

    private Boolean important;
    private Boolean pinned;

    private String priority;
    private String noteType;
    private String tags;

    private String courseCode;
    private String courseName;

    private List<AttachmentAiContext> attachments;
}