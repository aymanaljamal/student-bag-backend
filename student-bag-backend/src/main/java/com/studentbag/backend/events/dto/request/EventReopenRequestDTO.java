package com.studentbag.backend.events.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventReopenRequestDTO {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}