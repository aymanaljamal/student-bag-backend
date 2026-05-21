package com.studentbag.backend.chatbot.dto.context;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtaskAiContext {

    private Long id;
    private String title;
    private Boolean completed;
    private Integer orderIndex;
}