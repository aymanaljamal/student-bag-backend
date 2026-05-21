package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAiContext {

    private Long id;

    private String title;
    private String description;

    private LocalDateTime dueDateTime;

    private String priority;
    private String status;

    private Integer estimatedMinutes;

    private String courseCode;
    private String courseName;

    private List<String> labels;
    private List<SubtaskAiContext> subtasks;
    private List<AttachmentAiContext> attachments;
}