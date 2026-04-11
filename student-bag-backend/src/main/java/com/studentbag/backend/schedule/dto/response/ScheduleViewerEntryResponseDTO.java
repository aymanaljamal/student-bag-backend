package com.studentbag.backend.schedule.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleViewerEntryResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isAllDay;
    private String sourceType;
    private boolean isLocked;
    private String colorHex;
    private Integer courseSectionId;
    private Integer eventId;

    private Integer instructorId;
    private String instructorName;
}