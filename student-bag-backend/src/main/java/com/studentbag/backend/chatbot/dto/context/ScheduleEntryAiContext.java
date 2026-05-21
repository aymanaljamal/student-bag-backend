package com.studentbag.backend.chatbot.dto.context;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntryAiContext {

    private Long id;

    private String title;
    private String description;
    private String location;

    private String sourceType;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private Boolean isAllDay;

    private String courseCode;
    private String courseName;
    private String sectionNumber;
    private String instructorName;
    private String room;
    private String building;
    private String campus;
}