package com.studentbag.backend.schedule.dto;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class ClassSessionDTO {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private String building;
}