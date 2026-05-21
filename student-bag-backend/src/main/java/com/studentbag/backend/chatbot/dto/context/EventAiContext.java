package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAiContext {

    private Long id;

    private String title;
    private String description;
    private String eventType;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private String location;
    private String department;
    private String host;

    private Boolean requiresRegistration;
    private Boolean isOpportunity;

    private String companyName;
    private String roleTitle;
    private String field;
    private Boolean isPaid;
    private String workMode;
    private LocalDate applicationDeadline;
    private Integer durationWeeks;
}