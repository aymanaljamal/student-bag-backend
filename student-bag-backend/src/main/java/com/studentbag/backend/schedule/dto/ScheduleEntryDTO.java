package com.studentbag.backend.schedule.dto;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleEntryDTO {
    private Long id;
    private String title;
    private String location;
    private LocalDateTime start;
    private LocalDateTime end;
    private String colorHex;
    private String sourceType; // COURSE, MANUAL, EVENT
    private Boolean isLocked;
}